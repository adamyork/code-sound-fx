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
public class DefaultCodeSoundStateService implements CodeSoundStateService {

    private Optional<JsonDeserializer<JsonObject>> noteProvider = Optional.empty();
    private List<AnalysisResult> analysisResults;
    private List<Note> notes;
    private String selectedSource;

    @Override
    public List<AnalysisResult> getAnalysisResults() {
        return analysisResults;
    }

    @Override
    public void setAnalysisResults(final List<AnalysisResult> analysisResults) {
        this.analysisResults = analysisResults;
    }

    @Override
    public List<Note> getNotes() {
        return notes;
    }

    @Override
    public void setNotes(final List<Note> notes) {
        this.notes = notes;
    }

    @Override
    public String getSelectedSource() {
        return selectedSource;
    }

    @Override
    public void setSelectedSource(final String path) {
        selectedSource = path;
    }

    @Override
    public Optional<JsonDeserializer<JsonObject>> getNoteProvider() {
        return noteProvider;
    }

    @Override
    public void setNoteProvider(final Optional<JsonDeserializer<JsonObject>> noteProvider) {
        this.noteProvider = noteProvider;
    }

}
