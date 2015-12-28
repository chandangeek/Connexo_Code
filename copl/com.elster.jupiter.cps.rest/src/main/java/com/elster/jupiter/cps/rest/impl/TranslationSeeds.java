package com.elster.jupiter.cps.rest.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationSeeds implements TranslationKey {

    TYPE_BIGDECIMAL("java.math.BigDecimal", "BigDecimal"),
    TYPE_STRING("java.lang.String", "String"),
    TYPE_BOOLEAN("java.lang.Boolean", "Boolean"),
    TRUE_VALUE("true", "True"),
    FALSE_VALUE("false", "False"),;

    private String key;
    private final String defaultFormat;

    TranslationSeeds(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }
}