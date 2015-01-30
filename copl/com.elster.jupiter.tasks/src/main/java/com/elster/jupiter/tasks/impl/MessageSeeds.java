package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum MessageSeeds implements TranslationKey {

    NOT_UNIQUE(Constants.NOT_UNIQUE, "Must be unique");

    private final String key;
    private final String defaultFormat;

    MessageSeeds(String key, String defaultFormat) {
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

    public enum Constants {;
        public static final String NOT_UNIQUE = "NotUnique";
    }
}

