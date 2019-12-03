package com.github.adamyork.codesoundfx.view;

import com.github.adamyork.codesoundfx.GlobalStage;
import com.github.adamyork.codesoundfx.model.AnalysisResult;
import com.github.adamyork.codesoundfx.model.CodeSoundScene;
import com.github.adamyork.codesoundfx.service.CodeSoundStateService;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.lambda.Unchecked;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Created by Adam York on 10/27/2017.
 * Copyright 2017
 */
@Component
public class AnalyzeViewController implements Initializable {

    private static final Logger LOG = LogManager.getLogger(AnalyzeViewController.class);
    private static final int MAX_TOTAL_FILES = 1024;

    private final GlobalStage globalStage;
    private final CodeSoundStateService codeSoundStateService;
    private final MessageSource messageSource;

    @FXML
    private Button selectSourceButton;
    @FXML
    private Button selectedNoteProviderButton;
    @FXML
    private Button analyzeButton;
    @FXML
    private Button processButton;
    @FXML
    private Label selectedSourceLabel;
    @FXML
    private Label selectedNoteProviderLabel;
    @FXML
    private Label selectedDirectoryHeaderLabel;
    @FXML
    private Label selectedNoteProviderHeaderLabel;
    @FXML
    private TableView<AnalysisResult> analysisTable;

    private String selectedSourceDefaultValue;

    public AnalyzeViewController(@Qualifier("globalStage") final GlobalStage globalStage,
                                 @Qualifier("codeSoundState") final CodeSoundStateService codeSoundStateService,
                                 @Qualifier("messageSource") final MessageSource messageSource) {
        this.globalStage = globalStage;
        this.codeSoundStateService = codeSoundStateService;
        this.messageSource = messageSource;
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        selectedDirectoryHeaderLabel.setText(messageSource.getMessage("analyze.source.header",
                null, Locale.getDefault()));
        selectSourceButton.setText(messageSource.getMessage("analyze.source.button",
                null, Locale.getDefault()));
        selectedSourceDefaultValue = messageSource.getMessage("analyze.source.default",
                null, Locale.getDefault());
        selectedNoteProviderHeaderLabel.setText(messageSource.getMessage("analyze.note.provider.header",
                null, Locale.getDefault()));
        selectedNoteProviderButton.setText(messageSource.getMessage("analyze.note.provider.button",
                null, Locale.getDefault()));
        analyzeButton.setText(messageSource.getMessage("analyze.button.analyze",
                null, Locale.getDefault()));
        processButton.setText(messageSource.getMessage("analyze.button.process",
                null, Locale.getDefault()));
        selectSourceButton.setOnAction(this::selectSource);
        selectedNoteProviderButton.setOnAction(this::selectNoteProvider);
        analyzeButton.setOnAction(this::analyze);
        processButton.setOnAction(this::process);
        final boolean result = Optional.ofNullable(codeSoundStateService.getSelectedSource())
                .map(source -> {
                    selectedSourceLabel.setText(codeSoundStateService.getSelectedSource());
                    initializeResultTable(codeSoundStateService.getAnalysisResults());
                    analyzeButton.setDisable(true);
                    return true;
                })
                .orElseGet(() -> {
                    selectedSourceLabel.setText(selectedSourceDefaultValue);
                    analyzeButton.setDisable(true);
                    processButton.setDisable(true);
                    return false;
                });
        LOG.info("initialized with previous state" + result);
    }

    private void selectSource(final ActionEvent actionEvent) {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(messageSource.getMessage("analyze.dialog.source",
                null, Locale.getDefault()));
        final File projectDirectory = directoryChooser.showDialog(globalStage.getStage());
        final String selectedDirectory = Optional.ofNullable(projectDirectory)
                .map(File::getAbsolutePath)
                .orElseGet(() -> Optional.ofNullable(codeSoundStateService.getSelectedSource())
                        .orElse(selectedSourceDefaultValue));
        final boolean result = Optional.of(!selectedDirectory.equals(selectedSourceDefaultValue))
                .filter(bool -> bool)
                .map(bool -> {
                    selectedSourceLabel.setText(selectedDirectory);
                    codeSoundStateService.setSelectedSource(selectedDirectory);
                    analyzeButton.setDisable(false);
                    return true;
                })
                .orElse(false);
        LOG.info("source selected " + result);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void selectNoteProvider(final ActionEvent actionEvent) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("jar", "*.jar")
        );
        fileChooser.setTitle(messageSource.getMessage("analyze.dialog.jar",
                null, Locale.getDefault()));
        final File file = fileChooser.showOpenDialog(globalStage.getStage());
        final JsonDeserializer<JsonObject> noteProvider = Optional.ofNullable(file)
                .map(selectedJar -> {
                    final URL jarUrl = Unchecked.supplier(() -> file.toURI().toURL()).get();
                    final URLClassLoader childClassLoader = new URLClassLoader(new URL[]{jarUrl},
                            this.getClass().getClassLoader());
                    final JarFile jar = Unchecked.supplier(() -> new JarFile(file)).get();
                    final Enumeration<JarEntry> jarEntries = jar.entries();
                    return Collections.list(jarEntries)
                            .stream()
                            .filter(jarEntry -> jarEntry.getName().contains(".class"))
                            .map(jarEntry -> {
                                final Reflections reflections = new Reflections(jarUrl);
                                final String clazzName = reflections.getStore().get("SubTypesScanner").asMap()
                                        .get("com.google.gson.JsonDeserializer").stream().findFirst()
                                        .orElse("");
                                return Optional.of(!clazzName.isEmpty())
                                        .map(bool -> {
                                            final Class<JsonDeserializer> providerClazz = Unchecked
                                                    .supplier(() -> (Class<JsonDeserializer>) childClassLoader.loadClass(clazzName))
                                                    .get();
                                            final JsonDeserializer<JsonObject> instance = Unchecked
                                                    .supplier(() -> (JsonDeserializer<JsonObject>) providerClazz.getConstructor().newInstance())
                                                    .get();
                                            selectedNoteProviderLabel.setText(jarUrl.getPath() + jarUrl.getFile());
                                            return instance;
                                        })
                                        .orElse(null);
                            })
                            .findFirst()
                            .orElse(codeSoundStateService.getNoteProvider()
                                    .orElse(null));
                })
                .orElse(null);
        codeSoundStateService.setNoteProvider(Optional.ofNullable(noteProvider));
    }

    private void analyze(final ActionEvent actionEvent) {
        processButton.setDisable(false);
        final boolean result = parseSourceTree(selectedSourceLabel.getText());
        LOG.info("analysis completed " + result);
    }

    private void process(final ActionEvent actionEvent) {
        globalStage.navigateTo(CodeSoundScene.PROCESS_VIEW);
    }

    private boolean parseSourceTree(final String path) {
        final File currentFolder = new File(path);
        final Collection<File> files = FileUtils.listFiles(currentFolder,
                new RegexFileFilter(".*"), DirectoryFileFilter.DIRECTORY);
        return Optional.of(files.size() > MAX_TOTAL_FILES)
                .filter(bool -> bool)
                .map(bool -> {
                    final Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(messageSource.getMessage("analyze.error.tmf.title",
                            null, Locale.getDefault()));
                    alert.setHeaderText(messageSource.getMessage("analyze.error.tmf.header",
                            null, Locale.getDefault()));
                    alert.setContentText(messageSource.getMessage("analyze.error.tmf.content",
                            null, Locale.getDefault()));
                    alert.show();
                    codeSoundStateService.setSelectedSource(selectedSourceDefaultValue);
                    return false;
                })
                .orElseGet(() -> {
                    final List<AnalysisResult> analysisResultsList = files.stream()
                            .map(file -> {
                                final Optional<String> maybeMimeType = Optional
                                        .ofNullable(Unchecked.supplier(() -> Files.probeContentType(file.toPath())).get());
                                final Boolean keep = maybeMimeType
                                        .map(mimeType -> !mimeType.contains("image") && !mimeType.contains("octet-stream")
                                                && !mimeType.contains("x-"))
                                        .orElse(false);
                                return new AnalysisResult.Builder(FilenameUtils.getBaseName(file.getName()),
                                        FilenameUtils.getExtension(file.getName()),
                                        String.valueOf(file.length()),
                                        file.getAbsolutePath(),
                                        keep).build();
                            })
                            .collect(Collectors.toList());
                    codeSoundStateService.setAnalysisResults(analysisResultsList);
                    return initializeResultTable(analysisResultsList);
                });
    }

    @SuppressWarnings("unchecked")
    private boolean initializeResultTable(final List<AnalysisResult> analysisResultsList) {
        final ObservableList<AnalysisResult> analysisResults = FXCollections.observableList(analysisResultsList);
        final TableColumn<AnalysisResult, String> fileColumn = new TableColumn<>(messageSource.getMessage("analyze.result.column.file",
                null, Locale.getDefault()));
        fileColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        final TableColumn<AnalysisResult, String> typeColumn = new TableColumn<>(messageSource.getMessage("analyze.result.column.type",
                null, Locale.getDefault()));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        final TableColumn<AnalysisResult, String> sizeColumn = new TableColumn<>(messageSource.getMessage("analyze.result.column.size",
                null, Locale.getDefault()));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        analysisTable.setItems(analysisResults);
        analysisTable.getColumns().setAll(fileColumn, typeColumn, sizeColumn);
        analysisTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return true;
    }

}
