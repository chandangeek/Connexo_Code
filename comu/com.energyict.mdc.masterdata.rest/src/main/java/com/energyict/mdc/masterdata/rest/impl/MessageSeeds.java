/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    NO_LOAD_PROFILE_TYPE_FOUND(1, "NoLoadProfileTypeFound", "No LoadProfile type with id {0}",Level.SEVERE),
    NO_OBIS_TO_READING_TYPE_MAPPING_POSSIBLE(2, "NoObisToReadingTypeMappingPossible", "The OBIS code cannot be mapped to a reading type. Displaying the full reading type list", Level.WARNING),
    MAPPED_READING_TYPE_IS_IN_USE(3, "MappedReadingTypeIsInUse", "The OBIS code maps only to already used reading types. Displaying the full reading type list", Level.WARNING),

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