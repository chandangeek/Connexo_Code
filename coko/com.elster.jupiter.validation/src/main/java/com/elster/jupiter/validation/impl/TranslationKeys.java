package com.elster.jupiter.validation.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    MESSAGE_SPEC_SUBSCRIBER(ValidationServiceImpl.SUBSCRIBER_NAME, "Data validation"),
    ;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    private String key;
    private String defaultFormat;

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }
}
