package com.github.adamyork.codesoundfx.model;


import javafx.scene.paint.Color;

import java.util.*;

/**
 * Created by Adam York on 9/22/2019.
 * Copyright 2019
 */
@SuppressWarnings("WeakerAccess")
public class Note {

    public static final int A_NOTE = 69;
    public static final int Bb_NOTE = 70;
    public static final int B_NOTE = 71;
    public static final int C_NOTE = 60;
    public static final int CSharp_NOTE = 61;
    public static final int D_NOTE = 62;
    public static final int DSharp_NOTE = 63;
    public static final int E_NOTE = 64;
    public static final int F_NOTE = 65;
    public static final int FSharp_NOTE = 66;
    public static final int G_NOTE = 67;
    public static final int GSharp_NOTE = 68;

    public static final Map<Integer, Integer> OCTAVE_OFFSET = Map.ofEntries(
            new AbstractMap.SimpleEntry<>(0, -48),
            new AbstractMap.SimpleEntry<>(1, -36),
            new AbstractMap.SimpleEntry<>(2, -24),
            new AbstractMap.SimpleEntry<>(3, -12),
            new AbstractMap.SimpleEntry<>(4, 0),
            new AbstractMap.SimpleEntry<>(5, 12),
            new AbstractMap.SimpleEntry<>(6, 24),
            new AbstractMap.SimpleEntry<>(7, 36),
            new AbstractMap.SimpleEntry<>(8, 48),
            new AbstractMap.SimpleEntry<>(9, 60)
    );

    public static final Map<Integer, Color> OCTAVE_COLORS = Map.ofEntries(
            new AbstractMap.SimpleEntry<>(0, Color.BLACK),
            new AbstractMap.SimpleEntry<>(1, Color.DARKBLUE),
            new AbstractMap.SimpleEntry<>(2, Color.BLUE),
            new AbstractMap.SimpleEntry<>(3, Color.LIGHTBLUE),
            new AbstractMap.SimpleEntry<>(4, Color.LIGHTGREEN),
            new AbstractMap.SimpleEntry<>(5, Color.GREEN),
            new AbstractMap.SimpleEntry<>(6, Color.DARKGREEN),
            new AbstractMap.SimpleEntry<>(7, Color.VIOLET),
            new AbstractMap.SimpleEntry<>(8, Color.BLUEVIOLET),
            new AbstractMap.SimpleEntry<>(9, Color.DARKVIOLET)
    );

    public static List<Integer> NOTES = Arrays.asList(C_NOTE,
            CSharp_NOTE, D_NOTE, DSharp_NOTE, E_NOTE, F_NOTE, FSharp_NOTE, G_NOTE, GSharp_NOTE, A_NOTE, Bb_NOTE, B_NOTE);

    public static List<Integer> NOTE_LENGTHS = Arrays.asList(1000000, 750000, 50000, 25000, 0);

    private final int note;
    private final int duration;
    private final int octave;
    private final int velocity;
    private final int channel;
    private final String line;
    private final int lineLength;
    private final int offset;
    private final int max;
    private final int min;

    private Note(final Builder builder) {
        note = builder.note;
        duration = builder.duration;
        octave = builder.octave;
        velocity = builder.velocity;
        channel = builder.channel;
        line = builder.line;
        lineLength = builder.lineLength;
        offset = builder.offset;
        max = builder.max;
        min = builder.min;
    }

    public int getAdjustedNote() {
        final int octaveOffset = OCTAVE_OFFSET.get(octave);
        final int adjusted = note + octaveOffset;
        return Optional.of(adjusted < 0)
                .filter(bool -> bool)
                .map(bool -> 0)
                .orElseGet(() -> Optional.of(adjusted > 127)
                        .filter(bool -> bool)
                        .map(bool -> 127)
                        .orElse(adjusted));
    }

    public int getPatchProgram(final int numInstruments) {
        return Optional.of(lineLength > (numInstruments / 2))
                .filter(bool -> bool)
                .map(bool -> numInstruments / 2)
                .orElse(lineLength);
    }

    public static class Builder {

        private final int note;
        private final int duration;
        private final int octave;
        private final int velocity;
        private final int channel;
        private final String line;
        private final int lineLength;
        private final int offset;
        private final int max;
        private final int min;

        public Builder(final int note,
                       final int duration,
                       final int octave,
                       final int velocity,
                       final int channel,
                       final String line,
                       final int lineLength,
                       final int offset,
                       final int max,
                       final int min) {
            this.note = note;
            this.duration = duration;
            this.octave = octave;
            this.velocity = velocity;
            this.channel = channel;
            this.line = line;
            this.lineLength = lineLength;
            this.offset = offset;
            this.max = max;
            this.min = min;
        }

        public Note build() {
            return new Note(this);
        }

    }

    public int getNote() {
        return note;
    }

    public int getDuration() {
        return duration;
    }

    public int getOctave() {
        return octave;
    }

    public int getVelocity() {
        return velocity;
    }

    @SuppressWarnings("unused")
    public int getChannel() {
        return channel;
    }

    public String getLine() {
        return line;
    }

    public int getOffset() {
        return offset;
    }

    public int getMax() {
        return max;
    }

    public int getMin() {
        return min;
    }
}
