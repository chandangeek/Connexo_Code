package com.elster.insight.usagepoint.data.impl;


import com.elster.insight.usagepoint.data.UsagePointDataService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

import static java.util.logging.Level.SEVERE;

/**
 * Defines all the {@link MessageSeed}s of the usage point data module.
 *
 */
public enum MessageSeeds implements MessageSeed {
    LAST_CHECKED_CANNOT_BE_NULL(2079, Keys.LAST_CHECKED_CANNOT_BE_NULL, "The new last checked timestamp cannot be null"),
    LAST_CHECKED_AFTER_CURRENT_LAST_CHECKED(2080, Keys.LAST_CHECKED_AFTER_CURRENT_LAST_CHECKED, "The new last checked {2,date,yyyy-MM-dd HH:mm:ss} cannot be after current last checked {1,date,yyyy-MM-dd HH:mm:ss}"),
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat) {
        this(number, key, defaultFormat, SEVERE);
    }

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
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
        return level;
    }

    @Override
    public String getModule() {
        return UsagePointDataService.COMPONENT_NAME;
    }

    public static class Keys {
        public static final String LAST_CHECKED_CANNOT_BE_NULL = "lastChecked.null";
        public static final String LAST_CHECKED_AFTER_CURRENT_LAST_CHECKED = "lastChecked.after.currentLastChecked";
    }
}