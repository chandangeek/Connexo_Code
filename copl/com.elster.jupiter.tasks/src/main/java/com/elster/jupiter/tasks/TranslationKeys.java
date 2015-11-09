package com.elster.jupiter.tasks;

import com.elster.jupiter.nls.TranslationKey;


public enum TranslationKeys implements TranslationKey {
    SCHEDULED("recurrenttask.occurrence.scheduled", "Scheduled"),
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
