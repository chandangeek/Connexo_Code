package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed, TranslationKey {
    DELETE_TASK_STATUS_BUSY(1001, Keys.DELETE_TASK_STATUS_BUSY, "The estimation task cannot be removed because the task is running at this moment.", Level.SEVERE),
    DELETE_TASK_SQL_EXCEPTION(1002, Keys.DELETE_TASK_SQL_EXCEPTION, "Data export task {0} could not be removed. There was a problem accessing the database", Level.SEVERE);

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
        return EstimationService.COMPONENTNAME;
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

    public static final class Keys {
        private Keys(){}

        public static final String DELETE_TASK_STATUS_BUSY = "DeleteTaskStatusBusy";
        public static final String DELETE_TASK_SQL_EXCEPTION = "DeleteTaskSqlException";
    }
}
