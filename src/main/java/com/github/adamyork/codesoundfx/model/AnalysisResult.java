package com.github.adamyork.codesoundfx.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by Adam York on 9/22/2019.
 * Copyright 2019
 */
public class AnalysisResult {

    private final StringProperty name;
    private final StringProperty type;
    private final StringProperty size;
    private final String absolutePath;
    private final boolean keep;

    private AnalysisResult(final Builder builder) {
        name = new SimpleStringProperty(builder.name);
        type = new SimpleStringProperty(builder.type);
        size = new SimpleStringProperty(builder.size);
        absolutePath = builder.absolutePath;
        keep = builder.keep;
    }

    @SuppressWarnings("unused")
    public String getName() {
        return name.get();
    }

    @SuppressWarnings("unused")
    public String getType() {
        return type.get();
    }

    @SuppressWarnings("unused")
    public String getSize() {
        return size.get();
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public boolean isKeep() {
        return keep;
    }

    public static class Builder {

        private final String name;
        private final String type;
        private final String size;
        private final String absolutePath;
        private final boolean keep;

        public Builder(final String name,
                       final String type,
                       final String size,
                       final String absolutePath,
                       final boolean keep) {
            this.name = name;
            this.type = type;
            this.size = size;
            this.absolutePath = absolutePath;
            this.keep = keep;
        }

        public AnalysisResult build() {
            return new AnalysisResult(this);
        }
    }

}
