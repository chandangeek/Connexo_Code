package com.energyict.mdc.engine.impl.monitor;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-02-09 (11:51)
 */
public enum PrettyPrintTimeDurationTranslationKeys implements TranslationKey {

    DAY_PLURAL("PrettyPrintTimeDuration.day.plural", "{0} days"),
    DAY_SINGULAR("PrettyPrintTimeDuration.day.singular", "{0} day"),
    HOUR_PLURAL("PrettyPrintTimeDuration.hour.plural", "{0} hours"),
    HOUR_SINGULAR("PrettyPrintTimeDuration.hour.singular", "{0} hour"),
    MINUTE_PLURAL("PrettyPrintTimeDuration.minute.plural", "{0} minutes"),
    MINUTE_SINGULAR("PrettyPrintTimeDuration.minute.singular", "{0} minute"),
    MONTH_PLURAL("PrettyPrintTimeDuration.month.plural", "{0} months"),
    MONTH_SINGULAR("PrettyPrintTimeDuration.month.singular", "{0} month"),
    SECOND_PLURAL("PrettyPrintTimeDuration.second.plural", "{0} seconds"),
    SECOND_SINGULAR("PrettyPrintTimeDuration.second.singular", "{0} second"),
    YEAR_PLURAL("PrettyPrintTimeDuration.year.plural", "{0} years"),
    YEAR_SINGULAR("PrettyPrintTimeDuration.year.singular", "{0} year"),
    SEPARATOR("PrettyPrintTimeDuration.separator", ", "),
    LAST_SEPARATOR("PrettyPrintTimeDuration.lastSeparator", " and ");

    private final String key;
    private final String defaultFormat;

    PrettyPrintTimeDurationTranslationKeys(String key, String defaultFormat) {
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