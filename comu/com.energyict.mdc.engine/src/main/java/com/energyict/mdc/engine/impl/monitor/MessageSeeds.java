package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.engine.EngineService;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-02-09 (11:51)
 */
public enum MessageSeeds implements MessageSeed, TranslationKey {

    DAY_PLURAL(10_000, "PrettyPrintTimeDuration.day.plural", "{0} days"),
    day_singular(10_001, "PrettyPrintTimeDuration.day.singular", "{0} day"),
    HOUR_PLURAL(10_002, "PrettyPrintTimeDuration.hour.plural", "{0} hours"),
    HOUR_SINGULAR(10_003, "PrettyPrintTimeDuration.hour.singular", "{0} hour"),
    MINUTE_PLURAL(10_004, "PrettyPrintTimeDuration.minute.plural", "{0} minutes"),
    MINUTE_SINGULAR(10_005, "PrettyPrintTimeDuration.minute.singular", "{0} minute"),
    MONTH_PLURAL(10_006, "PrettyPrintTimeDuration.month.plural", "{0} months"),
    MONTH_SINGULAR(10_007, "PrettyPrintTimeDuration.month.singular", "{0} month"),
    SECOND_PLURAL(10_008, "PrettyPrintTimeDuration.second.plural", "{0} seconds"),
    SECOND_SINGULAR(10_009, "PrettyPrintTimeDuration.second.singular", "{0} second"),
    YEAR_PLURAL(10_010, "PrettyPrintTimeDuration.year.plural", "{0} years"),
    YEAR_SINGULAR(10_011, "PrettyPrintTimeDuration.year.singular", "{0} year"),
    SEPARATOR(10_012, "PrettyPrintTimeDuration.separator", ", "),
    LAST_SEPARATOR(10_013, "PrettyPrintTimeDuration.lastSeparator", " and ");

    private final int number;
    private final String key;
    private final String defaultFormat;

    MessageSeeds(int number, String key, String defaultFormat) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return Level.INFO;
    }

    @Override
    public String getModule() {
        return EngineService.COMPONENTNAME;
    }

}