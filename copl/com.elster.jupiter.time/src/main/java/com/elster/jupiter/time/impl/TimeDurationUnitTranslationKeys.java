/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.time.TimeDuration;

public enum TimeDurationUnitTranslationKeys implements TranslationKey {
    MILLISECONDS(TimeDuration.TimeUnit.MILLISECONDS.getDescription(), "millisecond(s)"),
    SECONDS(TimeDuration.TimeUnit.SECONDS.getDescription(), "second(s)"),
    MINUTES(TimeDuration.TimeUnit.MINUTES.getDescription(), "minute(s)"),
    HOURS(TimeDuration.TimeUnit.HOURS.getDescription(), "hour(s)"),
    DAYS(TimeDuration.TimeUnit.DAYS.getDescription(), "day(s)"),
    WEEKS(TimeDuration.TimeUnit.WEEKS.getDescription(), "week(s)"),
    MONTHS(TimeDuration.TimeUnit.MONTHS.getDescription(), "month(s)"),
    YEARS(TimeDuration.TimeUnit.YEARS.getDescription(), "year(s)"),
    ;

    private final String key;
    private final String format;

    TimeDurationUnitTranslationKeys(String description, String defaultFormat) {
        this.key = Constants.DESCRIPTION_TRANSLATION_KEY_PREFIX + description;
        this.format = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return format;
    }

    public interface Constants {
        String DESCRIPTION_TRANSLATION_KEY_PREFIX = "timeDuration.timeUnit.description.";
    }
}
