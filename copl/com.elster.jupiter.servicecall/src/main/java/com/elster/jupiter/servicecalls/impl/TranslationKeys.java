package com.elster.jupiter.servicecalls.impl;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Created by bvn on 2/4/16.
 */
public enum TranslationKeys implements TranslationKey {
    BLABLA("bla", "Bla bla"),
    ;

    private final String key;
    private final String defaultFormat;

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
