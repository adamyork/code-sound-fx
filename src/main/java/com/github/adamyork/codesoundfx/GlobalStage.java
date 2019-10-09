package com.github.adamyork.codesoundfx;

import com.github.adamyork.codesoundfx.model.CodeSoundScene;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Created by Adam York on 9/22/2019.
 * Copyright 2019
 */
public interface GlobalStage {

    Stage getStage();

    void setStage(final Stage stage);

    void setApplicationContext(final ConfigurableApplicationContext applicationContext);

    void navigateTo(final CodeSoundScene scene);

}
