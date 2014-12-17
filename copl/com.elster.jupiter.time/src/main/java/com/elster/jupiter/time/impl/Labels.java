package com.elster.jupiter.time.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum Labels implements TranslationKey {
    EVERY_MINUTE("tme.minute", "every minute"),
    EVERY_N_MINUTES("tme.minutes", "every {0} minutes"),
    EVERY_HOUR("tme.hour", "every hour"),
    EVERY_N_HOUR("tme.hours", "every {0} hours"),
    EVERY_DAY("tme.day", "every day"),
    EVERY_N_DAY("tme.days", "every {0} days"),
    EVERY_WEEK("tme.week", "every week"),
    EVERY_N_WEEKS("tme.weeks", "every {0} weeks"),
    EVERY_MONTH("tme.month", "every month"),
    EVERY_N_MONTHS("tme.months", "every {0} months"),
    EVERY_YEAR("tme.year", "every year"),
    EVERY_N_YEARS("tme.years", "every {0} years");

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
        return thesaurus.getString(key, defaultFormat);
    }
}