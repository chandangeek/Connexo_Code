package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    ;

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }
}
