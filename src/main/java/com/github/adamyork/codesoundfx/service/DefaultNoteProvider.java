package com.github.adamyork.codesoundfx.service;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.github.adamyork.codesoundfx.model.Note.NOTES;
import static com.github.adamyork.codesoundfx.model.Note.NOTE_LENGTHS;

public class DefaultNoteProvider implements JsonDeserializer<JsonObject> {

    @Override
    public JsonObject deserialize(final JsonElement json, final Type typeOfT,
                                  final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject input = (JsonObject) json;
        final String line = input.get("line").getAsString();
        final int lineLength = line.length();
        final int max = input.get("max").getAsInt();
        final int min = input.get("min").getAsInt();
        final JsonObject output = new JsonObject();
        final int computedNote = noteFromLineLength(lineLength, max, min);
        final int computedDuration = durationFromLineLength(lineLength);
        final int computedOctave = octaveFromLineLength(lineLength);
        final int computedVelocity = velocityFromLineLength(lineLength);
        final int computedChannel = channelFromLineLength(lineLength);
        final int computedOffset = noteOffsetFromLineLength(lineLength);
        final JsonElement noteElement = new JsonPrimitive(computedNote);
        output.add("note", noteElement);
        final JsonElement durationElement = new JsonPrimitive(computedDuration);
        output.add("duration", durationElement);
        final JsonElement octaveElement = new JsonPrimitive(computedOctave);
        output.add("octave", octaveElement);
        final JsonElement velocityElement = new JsonPrimitive(computedVelocity);
        output.add("velocity", velocityElement);
        final JsonElement channelElement = new JsonPrimitive(computedChannel);
        output.add("channel", channelElement);
        final JsonElement lineElement = new JsonPrimitive(line);
        output.add("line", lineElement);
        final JsonElement lineLengthElement = new JsonPrimitive(lineLength);
        output.add("lineLength", lineLengthElement);
        final JsonElement offsetElement = new JsonPrimitive(computedOffset);
        output.add("offset", offsetElement);
        final JsonElement maxElement = new JsonPrimitive(max);
        output.add("max", maxElement);
        final JsonElement minElement = new JsonPrimitive(min);
        output.add("min", minElement);
        return output;
    }

    private int noteFromLineLength(final int lineLength, final int max, final int min) {
        final int dist = max - min;
        final int step = (int) Math.ceil(dist / 12F);
        return IntStream.range(0, 12)
                .sequential()
                .mapToObj(index -> {
                    final int stepMin = min + (index * step);
                    final int stepMax = min + ((index + 1) * step);
                    final boolean within = lineLength >= stepMin && lineLength <= stepMax;
                    return Optional.of(within)
                            .filter(bool -> bool)
                            .map(bool -> NOTES.get(index))
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("line length cannot be paired to note"));
    }

    private int channelFromLineLength(final int lineLength) {
        final int val = Math.round((float) lineLength / 10F);
        return Optional.of(val < 0)
                .filter(bool -> bool)
                .map(bool -> 0)
                .orElseGet(() -> Optional.of(val > 15)
                        .filter(bool -> bool)
                        .map(bool -> 15)
                        .orElse(val));
    }

    private int velocityFromLineLength(final int lineLength) {
        return Optional.of(lineLength >= 127)
                .filter(bool -> bool)
                .map(bool -> 127)
                .orElse(Math.max(lineLength, 40));
    }

    private int octaveFromLineLength(final int lineLength) {
        final int val = Math.round((float) lineLength / 10F);
        return Optional.of(val < 0)
                .filter(bool -> bool)
                .map(bool -> 0)
                .orElseGet(() -> Optional.of(val > 9)
                        .filter(bool -> bool)
                        .map(bool -> 9)
                        .orElse(val));
    }

    private int durationFromLineLength(final int lineLength) {
        return (lineLength * 100000) / 2;
    }

    private int noteOffsetFromLineLength(final int lineLength) {
        if (lineLength == 0) {
            return NOTE_LENGTHS.get(0);
        }
        if (lineLength > 0 && lineLength <= 50) {
            return NOTE_LENGTHS.get(1);
        }
        if (lineLength > 50 && lineLength <= 100) {
            return NOTE_LENGTHS.get(2);
        }
        if (lineLength > 100 && lineLength <= 150) {
            return NOTE_LENGTHS.get(3);
        }
        return NOTE_LENGTHS.get(4);
    }


}
