/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    CAN_NOT_BE_EMPTY(1, Constants.CAN_NOT_BE_EMPTY, "This field is required", Level.SEVERE),
    DEVICE_GROUP_MUST_BE_UNIQUE(2, Constants.DEVICE_GROUP_MUST_BE_UNIQUE, "There is already a KPI for this device group", Level.SEVERE),
    USAGEPOINT_GROUP_AND_PURPOSE_MUST_BE_UNIQUE(3, Constants.USAGEPOINT_GROUP_AND_PURPOSE_MUST_BE_UNIQUE, "There is already a KPI for these usage point group and purpose", Level.SEVERE);

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
        return DataQualityKpiService.COMPONENTNAME;
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
        public static final String CAN_NOT_BE_EMPTY = "CanNotBeEmpty";
        public static final String DEVICE_GROUP_MUST_BE_UNIQUE = "DeviceGroupMustBeUnique";
        public static final String USAGEPOINT_GROUP_AND_PURPOSE_MUST_BE_UNIQUE = "UsagePointGroupAndPurposeMustBeUnique";
    }
}
