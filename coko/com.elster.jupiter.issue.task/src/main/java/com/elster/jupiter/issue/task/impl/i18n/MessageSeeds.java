/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.impl.i18n;

import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.issue.task.TaskIssueService;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    UNABLE_TO_CREATE_EVENT(1, "DeviceLifecycleIssueUnableToMapEvent", "Unable to create event from event payload: {0}", Level.SEVERE),
    INVALID_NUMBER_OF_ARGUMENTS(2, "invalid.number.of.arguments", "Invalid number of arguments {0}, expected {1} ", Level.SEVERE),
    INVALID_ARGUMENT(3, "invalid.argument", "Invalid argument {0}", Level.SEVERE),

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
        return TaskIssueService.COMPONENT_NAME;
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
}