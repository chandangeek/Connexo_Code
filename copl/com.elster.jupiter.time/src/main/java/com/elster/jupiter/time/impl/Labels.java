/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum Labels implements TranslationKey {
    EVERY_MINUTE("tme.minute", "Every minute"),
    EVERY_N_MINUTES("tme.minutes", "Every {0} minutes"),
    EVERY_HOUR("tme.hour", "Every hour"),
    EVERY_N_HOUR("tme.hours", "Every {0} hours"),
    EVERY_DAY("tme.day", "Every day"),
    EVERY_N_DAY("tme.days", "Every {0} days"),
    EVERY_WEEK("tme.week", "Every week"),
    EVERY_N_WEEKS("tme.weeks", "Every {0} weeks"),
    EVERY_MONTH("tme.month", "Every month"),
    EVERY_N_MONTHS("tme.months", "Every {0} months"),
    EVERY_YEAR("tme.year", "Every year"),
    EVERY_N_YEARS("tme.years", "Every {0} years");

    private final String key;
    private final String defaultFormat;

    Labels(String key, String defaultFormat) {
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

    public String translate(Thesaurus thesaurus) {
        return thesaurus.getFormat(this).format();
    }

}