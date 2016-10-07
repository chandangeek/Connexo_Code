package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    TIME_MINUTE("TimeMinute", "{0} minute"),
    TIME_MINUTES("TimeMinutes", "{0} minutes"),
    TIME_HOUR("TimeHour", "{0} hour"),
    TIME_DAY("TimeDay", "{0} day"),
    TIME_MONTH("TimeMonth", "{0} month");

    private final String key;
    private final String format;

    TranslationKeys(String key, String format) {
        this.key = key;
        this.format = format;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return format;
    }
}