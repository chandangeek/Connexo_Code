/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    NO_SUCH_ENDDEVICE_GROUP(1, "NoSuchDeviceGroup", "No end device group with id ''{0}''", Level.SEVERE),
    NO_SUCH_USAGEPOINT_GROUP(2, "NoSuchUsagePointGroup", "No usage point group with id ''{0}''", Level.SEVERE),
    NO_SUCH_METROLOGY_PURPOSE(3, "NoSuchDeviceGroup", "No metrology purpose with id ''{0}''", Level.SEVERE),
    CAN_NOT_BE_EMPTY(4, "CanNotBeEmpty", "This field is required", Level.SEVERE);

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    @Override
    public String getModule() {
        return DataQualityApplication.COMPONENT_NAME;
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
