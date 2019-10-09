package com.github.adamyork.codesoundfx.service;

import com.github.adamyork.codesoundfx.model.Note;
import org.jooq.lambda.tuple.Tuple2;

import java.util.List;

/**
 * Created by Adam York on 9/27/2019.
 * Copyright 2019
 */
public interface NoteProviderService {

    List<Note> processNotes(final List<Tuple2<String, Integer>> lineSummary,
                            final String path,
                            final int max,
                            final int min);

}
