package com.github.adamyork.codesoundfx.service;

import com.github.adamyork.codesoundfx.model.Note;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Adam York on 9/22/2019.
 * Copyright 2019
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class DefaultNoteProviderService implements NoteProviderService {

    private final CodeSoundStateService codeSoundStateService;
    private final JsonDeserializer<JsonObject> defaultNoteProvider;

    public DefaultNoteProviderService(@Qualifier("codeSoundStateService") final CodeSoundStateService codeSoundStateService) {
        this.codeSoundStateService = codeSoundStateService;
        defaultNoteProvider = new DefaultNoteProvider();
    }

    @Override
    public List<Note> processNotes(final List<Tuple2<String, Integer>> lineSummary,
                                   final String path,
                                   final int max,
                                   final int min) {
        return lineSummary.stream()
                .map(lineAndLength -> {
                    final JsonObject input = new JsonObject();
                    final JsonElement lineElement = new JsonPrimitive(lineAndLength.v1);
                    input.add("line", lineElement);
                    final JsonElement pathElement = new JsonPrimitive(path);
                    input.add("path", pathElement);
                    final JsonElement maxElement = new JsonPrimitive(max);
                    input.add("max", maxElement);
                    final JsonElement minElement = new JsonPrimitive(min);
                    input.add("min", minElement);
                    final JsonDeserializer<JsonObject> processor = codeSoundStateService.getNoteProvider()
                            .orElse(defaultNoteProvider);
                    final JsonObject output = processor.deserialize(input, JsonObject.class, null);
                    return new Note.Builder(output.get("note").getAsInt(),
                            output.get("duration").getAsInt(),
                            output.get("octave").getAsInt(),
                            output.get("velocity").getAsInt(),
                            output.get("channel").getAsInt(),
                            output.get("line").getAsString(),
                            output.get("lineLength").getAsInt(),
                            output.get("offset").getAsInt(),
                            output.get("max").getAsInt(),
                            output.get("min").getAsInt())
                            .build();
                })
                .collect(Collectors.toList());
    }

}
