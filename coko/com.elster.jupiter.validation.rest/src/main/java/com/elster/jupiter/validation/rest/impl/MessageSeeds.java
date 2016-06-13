package com.elster.jupiter.validation.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    RUN_TASK_CONCURRENT_TITLE(4, "RunTaskConcurrentTitle", "Failed to run ''{0}''", Level.SEVERE),
    RUN_TASK_CONCURRENT_BODY(5, "RunTaskConcurrentMessage", "{0} has changed since the page was last updated.", Level.SEVERE),
    NO_SUCH_DEVICE_GROUP(227, "NoSuchDeviceGroup", "No end device group with id ''{0}''", Level.SEVERE);

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
        return ValidationApplication.COMPONENT_NAME;
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
}
