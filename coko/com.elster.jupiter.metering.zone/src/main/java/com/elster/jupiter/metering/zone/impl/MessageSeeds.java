/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.zone.impl;

import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed, TranslationKey {

    ZONE_TYPE_NAME_NOT_UNIQUE(1, Constants.ZONE_TYPE_NAME_NOT_UNIQUE, "Zone type name must be unique", Level.SEVERE),
    ZONE_TYPE_NAME_REQUIRED(2, Constants.ZONE_TYPE_NAME_REQUIRED, "Zone type name is required", Level.SEVERE),
    ZONE_TYPE_APP_REQUIRED(3, Constants.ZONE_TYPE_APP_REQUIRED, "Zone type application is required", Level.SEVERE),
    ZONE_NAME_NOT_UNIQUE(4, Constants.ZONE_NAME_NOT_UNIQUE, "Zone name must be unique", Level.SEVERE),
    ZONE_NAME_REQUIRED(5, Constants.ZONE_NAME_REQUIRED, "Zone name is required", Level.SEVERE),
    FIELD_SIZE_BETWEEN_1_AND_80(6, Constants.FIELD_SIZE_BETWEEN_1_AND_80, "This field''s text length should be between 1 and 80 symbols", Level.SEVERE),
    FIELD_SIZE_BETWEEN_1_AND_10(7, Constants.FIELD_SIZE_BETWEEN_1_AND_80, "This field''s text length should be between 1 and 10 symbols", Level.SEVERE),;

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
        return MeteringZoneService.COMPONENTNAME;
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
        public static final String ZONE_TYPE_NAME_NOT_UNIQUE = "zoneTypeNameNotUnique";
        public static final String ZONE_TYPE_NAME_REQUIRED = "zoneTypeNameCanNotBeEmpty";
        public static final String ZONE_TYPE_APP_REQUIRED = "zoneTypeAppCanNotBeEmpty";

        public static final String ZONE_NAME_NOT_UNIQUE = "zoneNameNotUnique";
        public static final String ZONE_NAME_REQUIRED = "zoneNameCanNotBeEmpty";
        public static final String FIELD_SIZE_BETWEEN_1_AND_80 = "fieldSizeBetween80";
        public static final String FIELD_SIZE_BETWEEN_1_AND_10 = "fieldSizeBetween10";
    }
}
