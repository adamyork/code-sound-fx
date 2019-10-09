package com.github.adamyork.codesoundfx;

import com.github.adamyork.codesoundfx.model.CodeSoundScene;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Created by Adam York on 10/22/2017.
 * Copyright 2017
 */
@SpringBootApplication
public class Main extends Application {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        System.setProperty("java.awt.headless", "false");
        applicationContext = SpringApplication.run(Main.class);
    }

    @Override
    public void start(final Stage stage) {
        final String version = applicationContext.getBeanFactory().resolveEmbeddedValue("${info.build.version}");
        stage.setTitle("CodeSound :: v" + version);
        final DefaultGlobalStage globalStage = applicationContext.getBean(DefaultGlobalStage.class);
        globalStage.setStage(stage);
        globalStage.setApplicationContext(applicationContext);
        globalStage.navigateTo(CodeSoundScene.ANALYZE_VIEW);
    }

    @Override
    public void stop() {
        applicationContext.close();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
