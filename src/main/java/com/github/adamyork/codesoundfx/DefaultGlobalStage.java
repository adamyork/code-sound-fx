package com.github.adamyork.codesoundfx;

import com.github.adamyork.codesoundfx.model.CodeSoundScene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jooq.lambda.Unchecked;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Created by Adam York on 2/26/2017.
 * Copyright 2017
 */
public class DefaultGlobalStage implements GlobalStage {

    private Stage stage;
    private ConfigurableApplicationContext applicationContext;

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public void setStage(final Stage stage) {
        this.stage = (this.stage == null) ? this.stage = stage : this.stage;
    }

    @Override
    public void setApplicationContext(final ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void navigateTo(final CodeSoundScene scene) {
        switch (scene) {
            case ANALYZE_VIEW:
                final FXMLLoader analyzeViewLoader = new FXMLLoader(getClass().getClassLoader()
                        .getResource("analyzeView.fxml"));
                analyzeViewLoader.setControllerFactory(applicationContext::getBean);
                final Parent analyzeRoot = (Parent) Unchecked.function(o -> analyzeViewLoader.load()).apply(null);
                final Scene analyzeScene = new Scene(analyzeRoot, 810, 600);
                stage.setScene(analyzeScene);
                stage.setResizable(false);
                stage.show();
                break;
            case PROCESS_VIEW:
                final FXMLLoader processViewLoader = new FXMLLoader(getClass().getClassLoader()
                        .getResource("processView.fxml"));
                processViewLoader.setControllerFactory(applicationContext::getBean);
                final Parent processRoot = (Parent) Unchecked.function(o -> processViewLoader.load()).apply(null);
                final Scene processScene = new Scene(processRoot, 810, 600);
                stage.setScene(processScene);
                stage.setResizable(false);
                stage.show();
                break;
        }
    }

}

