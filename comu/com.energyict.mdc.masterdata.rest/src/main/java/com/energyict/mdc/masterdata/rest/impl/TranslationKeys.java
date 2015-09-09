package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    TIME_MINUTE("TimeMinute", "%s minute"),
    TIME_MINUTES("TimeMinutes", "%s minutes"),
    TIME_HOUR("TimeHour", "%s hour"),
    TIME_DAY("TimeDay", "%s day"),
    TIME_MONTH("TimeMonth", "%s month");

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