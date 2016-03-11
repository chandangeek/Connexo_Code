package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import java.text.MessageFormat;
import java.util.Locale;

public class SimpleNlsMessageFormat implements NlsMessageFormat {
    private final String defaultFormat;

    public SimpleNlsMessageFormat(TranslationKey translationKey) {
        this.defaultFormat = translationKey.getDefaultFormat();
    }

    public SimpleNlsMessageFormat(MessageSeed messageSeed) {
        this.defaultFormat = messageSeed.getDefaultFormat();
    }

    public SimpleNlsMessageFormat(String defaultFormat) {
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String format(Object... args) {
        return MessageFormat.format(this.defaultFormat, args);
    }

    @Override
    public String format(Locale locale, Object... args) {
        return MessageFormat.format(this.defaultFormat, args);
    }

}