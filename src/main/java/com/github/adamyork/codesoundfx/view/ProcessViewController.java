package com.github.adamyork.codesoundfx.view;

import com.github.adamyork.codesoundfx.GlobalStage;
import com.github.adamyork.codesoundfx.model.AnalysisResult;
import com.github.adamyork.codesoundfx.model.CodeSoundScene;
import com.github.adamyork.codesoundfx.model.Note;
import com.github.adamyork.codesoundfx.service.CodeSoundStateService;
import com.github.adamyork.codesoundfx.service.NoteProviderService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sound.midi.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Adam York on 9/22/2019.
 * Copyright 2019
 */
@Component
public class ProcessViewController implements Initializable {

    private static final Logger LOG = LogManager.getLogger(ProcessViewController.class);

    private static final int CANVAS_WIDTH = 780;
    private static final int CANVAS_HEIGHT = 60;
    private static final int TICK_WIDTH = 5;
    private static final int TICK_HEIGHT = 5;
    private static final long ONE_SECOND_IN_MICRO_LONG = 1000000L;
    private static final int ONE_SECOND_IN_MICRO = 1000000;

    private final GlobalStage globalStage;
    private final CodeSoundStateService codeSoundStateService;
    private final NoteProviderService noteProviderService;

    @FXML
    private AnchorPane midiVisAnchorPane;
    @FXML
    private Button listenButton;
    @FXML
    private Button backButton;
    @FXML
    private Label totalFilesValueLabel;
    @FXML
    private Label maxLineLengthValueLabel;
    @FXML
    private Label minLineLengthValueLabel;
    @FXML
    private Label avgLineLengthValueLabel;
    @FXML
    private Label omittedValueLabel;

    private Synthesizer synthesizer;
    private Receiver receiver;

    public ProcessViewController(@Qualifier("globalStage") final GlobalStage globalStage,
                                 @Qualifier("codeSoundState") final CodeSoundStateService codeSoundStateService,
                                 @Qualifier("noteProviderService") final NoteProviderService noteProviderService) {
        this.globalStage = globalStage;
        this.codeSoundStateService = codeSoundStateService;
        this.noteProviderService = noteProviderService;
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        listenButton.setOnAction(this::generateMidi);
        backButton.setOnAction(this::back);
        final boolean result = parseFiles();
        LOG.info("files parsed " + result);
    }

    @SuppressWarnings("unchecked")
    private boolean parseFiles() {
        final int filesOmitted = (int) codeSoundStateService.getAnalysisResults()
                .stream()
                .filter(analysisResult -> !analysisResult.isKeep())
                .count();
        final List<Note> allNotes = codeSoundStateService.getAnalysisResults()
                .stream()
                .filter(AnalysisResult::isKeep)
                .map(analysisResult -> {
                    final List<Tuple2<String, Integer>> lineSummary = Unchecked.supplier(() -> Files.readAllLines(Paths.get(analysisResult
                            .getAbsolutePath()), StandardCharsets.UTF_8)).get().stream()
                            .map(line -> Tuple.tuple(line, line.length()))
                            .collect(Collectors.toList());
                    final Integer minLineLength = lineSummary.stream()
                            .map(summary -> summary.v2)
                            .min(Comparator.comparing(Integer::intValue))
                            .orElse(0);
                    final Integer maxLineLength = lineSummary.stream()
                            .map(summary -> summary.v2)
                            .max(Comparator.comparing(Integer::intValue))
                            .orElse(0);
                    return noteProviderService.processNotes(lineSummary, analysisResult.getAbsolutePath(),
                            maxLineLength, minLineLength);
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        final int max = allNotes.stream()
                .max(Comparator.comparingInt(Note::getMax))
                .map(Note::getMax)
                .orElse(0);
        final int min = allNotes.stream()
                .min(Comparator.comparingInt(Note::getMin))
                .map(Note::getMin)
                .orElse(0);
        final double average = allNotes.stream()
                .map(note -> note.getLine().length())
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);
        totalFilesValueLabel.setText(String.valueOf(codeSoundStateService.getAnalysisResults().size()));
        maxLineLengthValueLabel.setText(String.valueOf(max));
        minLineLengthValueLabel.setText(String.valueOf(min));
        avgLineLengthValueLabel.setText(String.valueOf(average));
        omittedValueLabel.setText(String.valueOf(filesOmitted));
        Unchecked.consumer((notesList) -> {
            final List<Note> casted = (List<Note>) notesList;
            final int visualizationGenerated = generateVisualization(casted);
            LOG.info("visualization generated " + visualizationGenerated);
            codeSoundStateService.setNotes(casted);
        }).accept(allNotes);
        return true;
    }

    private int generateVisualization(final List<Note> notes) {
        final Map<Integer, Integer> nextX = new HashMap<>();
        nextX.put(1, 0);
        final Map<Integer, Integer> canvasIndexMap = new HashMap<>();
        canvasIndexMap.put(0, 1);
        final Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        midiVisAnchorPane.getChildren().add(canvas);
        midiVisAnchorPane.setPrefHeight(midiVisAnchorPane.getChildren().size() * CANVAS_HEIGHT);
        return IntStream.range(1, notes.size())
                .sequential()
                .map(index -> {
                    final int canvasIndex = canvasIndexMap.get(0);
                    final Note note = notes.get(index);
                    final int xpos = nextX.get(canvasIndexMap.get(0));
                    final int yPos = getYposFromNote(note) * TICK_HEIGHT;
                    final int width = getWidth(note);
                    final Canvas currentCanvas = (Canvas) midiVisAnchorPane.getChildren().get(canvasIndex - 1);
                    final GraphicsContext graphicsContext = currentCanvas
                            .getGraphicsContext2D();
                    graphicsContext.setFill(Note.OCTAVE_COLORS.get(note.getOctave()));
                    graphicsContext.fillRect(xpos, yPos, width, TICK_HEIGHT);
                    return Optional.of(xpos + width >= CANVAS_WIDTH)
                            .filter(bool -> bool)
                            .map(bool -> {
                                final int nextCanvasY = (midiVisAnchorPane.getChildren().size() + 1) * CANVAS_HEIGHT;
                                final Canvas nextCanvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
                                nextCanvas.setLayoutY(nextCanvasY);
                                midiVisAnchorPane.getChildren().add(nextCanvas);
                                midiVisAnchorPane.setPrefHeight(nextCanvasY + CANVAS_HEIGHT);
                                nextX.put(canvasIndex + 1, 0);
                                canvasIndexMap.put(0, canvasIndex + 1);
                                return 0;
                            })
                            .orElseGet(() -> {
                                nextX.put(canvasIndex, xpos + width);
                                return 0;
                            });
                }).sum();
    }

    private int getWidth(final Note note) {
        return (TICK_WIDTH * (note.getDuration() / ONE_SECOND_IN_MICRO)) + TICK_WIDTH;
    }

    private void generateMidi(final ActionEvent actionEvent) {
        Unchecked.consumer((p) -> {
            final List<Note> notes = codeSoundStateService.getNotes();
            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            receiver = synthesizer.getReceiver();
            final List<Instrument> allInstruments = Arrays.asList(synthesizer.getAvailableInstruments());
            final int bankZeroInstrumentCount = (int) allInstruments.stream()
                    .filter(instrument -> instrument.getPatch().getBank() == 0)
                    .count();
            IntStream.range(1, notes.size())
                    .sequential()
                    .forEach(index -> {
                        final Note note = notes.get(index);
                        Unchecked.consumer((n) -> {
                            final long noteStart = (ONE_SECOND_IN_MICRO_LONG * index) - (note.getOffset() * index);
                            final long noteEnd = noteStart + note.getDuration();
                            final ShortMessage changeMessage = new ShortMessage();
                            changeMessage.setMessage(ShortMessage.PROGRAM_CHANGE, note.getChannel(),
                                    note.getPatchProgram(bankZeroInstrumentCount), 0);
                            final ShortMessage onMessage = new ShortMessage();
                            onMessage.setMessage(ShortMessage.NOTE_ON, note.getChannel(), note.getAdjustedNote(),
                                    note.getVelocity());
                            final ShortMessage offMessage = new ShortMessage();
                            offMessage.setMessage(ShortMessage.NOTE_OFF, note.getChannel(), note.getAdjustedNote(),
                                    note.getVelocity());
                            receiver.send(changeMessage, noteStart);
                            receiver.send(onMessage, noteStart);
                            receiver.send(offMessage, noteEnd);
                        }).accept(null);
                    });
        }).accept(null);
    }

    private int getYposFromNote(final Note note) {
        switch (note.getNote()) {
            case Note.C_NOTE:
                return 11;
            case Note.CSharp_NOTE:
                return 10;
            case Note.D_NOTE:
                return 9;
            case Note.DSharp_NOTE:
                return 8;
            case Note.E_NOTE:
                return 7;
            case Note.F_NOTE:
                return 6;
            case Note.FSharp_NOTE:
                return 5;
            case Note.G_NOTE:
                return 4;
            case Note.GSharp_NOTE:
                return 3;
            case Note.A_NOTE:
                return 2;
            case Note.Bb_NOTE:
                return 1;
            case Note.B_NOTE:
            default:
                return 0;
        }
    }

    @SuppressWarnings("unused")
    private void back(final ActionEvent actionEvent) {
        final Boolean closeResult = Optional.ofNullable(receiver)
                .map(rec -> {
                    receiver.close();
                    synthesizer.close();
                    return true;
                })
                .orElse(false);
        globalStage.navigateTo(CodeSoundScene.ANALYZE_VIEW);
    }


}
