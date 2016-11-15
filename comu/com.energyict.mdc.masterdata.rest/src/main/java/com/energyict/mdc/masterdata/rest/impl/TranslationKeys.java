package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.time.TimeDuration;

public enum TranslationKeys implements TranslationKey {

    TIME_MINUTES(TimeDuration.TimeUnit.MINUTES.name(), "{0} minutes"),
    TIME_HOUR(TimeDuration.TimeUnit.HOURS.name(), "{0} hour"),
    TIME_DAY(TimeDuration.TimeUnit.DAYS.name(), "{0} day"),
    TIME_MONTH(TimeDuration.TimeUnit.MONTHS.name(), "{0} month");

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

    public static TranslationKeys getByKey(String key) {
        for(TranslationKeys translationKey: TranslationKeys.values()) {
            if(translationKey.getKey().equals(key)) {
                return translationKey;
            }
        }
        return null;
    }
}