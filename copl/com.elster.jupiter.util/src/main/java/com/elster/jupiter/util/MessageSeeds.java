/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

/**
 * Enumeration of the exception types in UTL module.
 */
public enum MessageSeeds implements MessageSeed {
    NO_SUCH_PROPERTY(1001, "Bean ''{0}'' has no property named ''{1}''."),
    BEAN_EVALUATION_FAILED(1002, "Exception occurred while evaluating bean {0}"),
    INVALID_CRON_EXPRESSION(1003, "Invalid cron expression : ''{0}''"),
    JSON_DESERIALIZATION_FAILED(1004, "Could not deserialize ''{0}'' to an instance of {1}"),
    JSON_SERIALIZATION_FAILED(1005, "Failed to serialize object : ''{0}''"),
    JSON_GENERATION_FAILED(1006, "Failed to generate json for {0}"),
    NO_SUCH_PROPERTY_ON_CLASS(1007, "Bean class ''{0}'' has no property named ''{1}''."),
    BEAN_EVALUATION_FAILED_ON_CLASS(1008, "Exception occurred while evaluating bean class {0}");

    private final int number;
    private final String defaultFormat;

    MessageSeeds(int number, String defaultFormat) {
        this.number = number;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getModule() {
        return "UTL";
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return name();
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }
}
