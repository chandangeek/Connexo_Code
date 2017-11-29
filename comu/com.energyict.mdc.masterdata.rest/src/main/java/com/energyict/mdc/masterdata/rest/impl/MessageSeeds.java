/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    NO_LOAD_PROFILE_TYPE_FOUND(1, "NoLoadProfileTypeFound", "No LoadProfile type with id {0}",Level.SEVERE),
    INVALID_CIM_OBIS_MAPPING(2, "InvalidCimObisMapping", "Cannot map the selected reading type to an obis code", Level.WARNING ),
    INVALID_OBIS_CIM_MAPPING(3, "InvalidObisCimMapping2", "Cannot map the selected obis code to a reading type", Level.WARNING ),
    INVALID_OBIS_CODE(4, "InvalidObisCode", "Invalid obis code", Level.WARNING)
    ;
    private final int number;
    private final String key;
    private final String format;
    private final Level level;

    MessageSeeds(int number, String key, String format, Level level) {
        this.number = number;
        this.key = key;
        this.format = format;
        this.level = level;
    }

    @Override
    public String getModule() {
        return MasterDataApplication.COMPONENT_NAME;
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
        return format;
    }

    @Override
    public Level getLevel() {
        return level;
    }

}