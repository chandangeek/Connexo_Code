package com.elster.jupiter.data.lifecycle.impl;

import com.elster.jupiter.data.lifecycle.LifeCycleCategoryKind;
import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed, TranslationKey{
    LIFE_CYCLE_KIND_INTERVAL (1, Constants.DATA_LIFECYCLE_CATEGORY_NAME_PREFIX + LifeCycleCategoryKind.INTERVAL.name(), "Interval data", Level.INFO),
    LIFE_CYCLE_KIND_DAILY (2, Constants.DATA_LIFECYCLE_CATEGORY_NAME_PREFIX + LifeCycleCategoryKind.DAILY.name(), "Day / Month profiles", Level.INFO),
    LIFE_CYCLE_KIND_REGISTER (3, Constants.DATA_LIFECYCLE_CATEGORY_NAME_PREFIX + LifeCycleCategoryKind.REGISTER.name(), "Register data", Level.INFO),
    LIFE_CYCLE_KIND_ENDDEVICEEVENT (4, Constants.DATA_LIFECYCLE_CATEGORY_NAME_PREFIX + LifeCycleCategoryKind.ENDDEVICEEVENT.name(), "Event data", Level.INFO),
    LIFE_CYCLE_KIND_LOGGING (5, Constants.DATA_LIFECYCLE_CATEGORY_NAME_PREFIX + LifeCycleCategoryKind.LOGGING.name(), "Logging data", Level.INFO),
    LIFE_CYCLE_KIND_JOURNAL (6, Constants.DATA_LIFECYCLE_CATEGORY_NAME_PREFIX + LifeCycleCategoryKind.JOURNAL.name(), "Journal tables", Level.INFO),
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    @Override
    public String getModule() {
        return LifeCycleService.COMPONENTNAME;
    }

    @Override
    public int getNumber() {
        return this.number;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

    public static class Constants {
        public static final String DATA_LIFECYCLE_CATEGORY_NAME_PREFIX = "data.lifecycle.category.";
    }
}
