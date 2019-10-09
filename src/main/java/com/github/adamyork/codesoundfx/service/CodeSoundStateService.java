package com.github.adamyork.codesoundfx.service;

import com.github.adamyork.codesoundfx.model.AnalysisResult;
import com.github.adamyork.codesoundfx.model.Note;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Optional;

/**
 * Created by Adam York on 9/22/2019.
 * Copyright 2019
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public interface CodeSoundStateService {

    List<AnalysisResult> getAnalysisResults();

    void setAnalysisResults(final List<AnalysisResult> analysisResults);

    List<Note> getNotes();

    void setNotes(final List<Note> notes);

    String getSelectedSource();

    void setSelectedSource(final String path);

    Optional<JsonDeserializer<JsonObject>> getNoteProvider();

    void setNoteProvider(final Optional<JsonDeserializer<JsonObject>> noteProvider);

}
