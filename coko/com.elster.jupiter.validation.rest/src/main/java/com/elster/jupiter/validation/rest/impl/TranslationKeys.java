package com.elster.jupiter.validation.rest.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey{
    SCHEDULED("validationtask.occurrence.scheduled", "Scheduled"),
    ON_REQUEST("validationtask.occurrence.onrequest", "On Request");
    ;

    private String key;
    private String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }
}
