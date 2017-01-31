/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    FLOW_CONNECTION_ERROR(1001, "error.flow.unavailable", "Cannot connect to Flow; HTTP error {0}.", Level.SEVERE),
    FLOW_INVALID_RESPONSE_ERROR(1002, "error.flow.invalid.response", "Invalid response received, please check your Flow version.", Level.SEVERE),
    FIELD_CAN_NOT_BE_EMPTY(1, Constants.FIELD_CAN_NOT_BE_EMPTY, "This field is required", Level.SEVERE);

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
        return BpmService.COMPONENTNAME;
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

    public enum Constants {
        ;
        public static final String FIELD_CAN_NOT_BE_EMPTY = "CanNotBeEmpty";
    }
}
