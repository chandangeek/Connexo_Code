package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.nls.TranslationKey;

import java.time.DayOfWeek;

/**
 * Created by igh on 18/04/2016.
 */
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
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }
}
