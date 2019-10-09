package com.github.adamyork.codesoundfx;

import com.github.adamyork.codesoundfx.service.CodeSoundStateService;
import com.github.adamyork.codesoundfx.service.DefaultCodeSoundStateService;
import com.github.adamyork.codesoundfx.service.DefaultNoteProviderService;
import com.github.adamyork.codesoundfx.service.NoteProviderService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * Created by Adam York on 9/22/2019.
 * Copyright 2019
 */
@Configuration
public class AppConfig {

    @Bean("globalStage")
    public GlobalStage globalStage() {
        return new DefaultGlobalStage();
    }

    @Bean("codeSoundState")
    public CodeSoundStateService codeSoundState() {
        return new DefaultCodeSoundStateService();
    }

    @Bean("noteProviderService")
    public NoteProviderService noteProviderService(@Qualifier("codeSoundState") final CodeSoundStateService codeSoundStateService) {
        return new DefaultNoteProviderService(codeSoundStateService);
    }

    @Bean("messageSource")
    public MessageSource messageSource() {
        final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

}
