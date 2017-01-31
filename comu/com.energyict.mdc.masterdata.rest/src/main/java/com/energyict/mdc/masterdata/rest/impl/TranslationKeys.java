/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.time.TimeDuration;

import java.util.Arrays;

public enum TranslationKeys implements TranslationKey {

    TIME_MINUTE("MINUTE", "{0} minute", true),
    TIME_MINUTES(TimeDuration.TimeUnit.MINUTES.name(), "{0} minutes", false),
    TIME_HOUR("HOUR", "{0} hour", true),
    TIME_HOURS(TimeDuration.TimeUnit.HOURS.name(), "{0} hours", false),
    TIME_DAY("DAY", "{0} day", true),
    TIME_DAYS(TimeDuration.TimeUnit.DAYS.name(), "{0} days", false),
    TIME_MONTH("MONTH", "{0} month", true),
    TIME_MONTHS(TimeDuration.TimeUnit.MONTHS.name(), "{0} months", false);

    private final String key;
    private final String format;

    TranslationKeys(String key, String format, boolean singular) {
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
        return Arrays.stream(TranslationKeys.values()).filter((k) -> k.getKey().equals(key)).findFirst().orElse(null);
    }
}