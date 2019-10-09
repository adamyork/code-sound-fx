package com.github.adamyork.codesound.sample;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Random;

/**
 * Created by Adam York on 9/30/2019.
 * Copyright 2019
 */
@SuppressWarnings("unused")
public class RandomNoteProvider implements JsonDeserializer<JsonObject> {

    @Override
    public JsonObject deserialize(final JsonElement json, final Type typeOfT,
                                  final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject input = (JsonObject) json;
        final String line = input.get("line").getAsString();
        final int lineLength = line.length();
        final int max = input.get("max").getAsInt();
        final int min = input.get("min").getAsInt();
        final JsonObject output = new JsonObject();
        final int computedNote = getRandomBetween(60, 72);
        final int computedDuration = 100000;
        final int computedOctave = getRandomBetween(0, 9);
        final int computedVelocity = getRandomBetween(40, 127);
        final int computedChannel = getRandomBetween(0, 15);
        final int computedOffset = 0;
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

    private int getRandomBetween(final int min, final int max) {
        final Random random = new Random();
        return random.nextInt(max - min) + min;
    }
}
