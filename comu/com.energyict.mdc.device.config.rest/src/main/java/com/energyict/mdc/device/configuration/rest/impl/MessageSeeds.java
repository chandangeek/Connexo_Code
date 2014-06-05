package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    FIELD_IS_REQUIRED(1, "DCR.RequiredField", "Field is required", Level.SEVERE),
    PROTOCOL_INVALID_NAME(2,"DCR.deviceType.no.such.protocol", "A protocol with name ''{0}'' does not exist",Level.SEVERE),
    NO_LOGBOOK_TYPE_ID_FOR_ADDING(3,"DCR.NoLogBookTypeIdForAdding", "User should specify ids of LogBook Type for adding",Level.SEVERE),
    NO_LOGBOOK_TYPE_FOUND(4, "DCR.NoLogBookTypeFound", "No LogBook type with id {0}",Level.SEVERE),
    NO_LOGBOOK_SPEC_FOUND(5, "DCR.NoLogBookSpecFound", "No LogBook configuration with id {0}",Level.SEVERE),
    NO_LOAD_PROFILE_TYPE_ID_FOR_ADDING(6, "DCR.NoLoadProfileTypeIdForAdding", "User should specify ids of Load Profile Type for adding",Level.SEVERE),
    NO_LOAD_PROFILE_TYPE_FOUND(7, "DCR.NoLoadProfileTypeFound", "No Load Profile type with id {0}",Level.SEVERE),
    NO_PHENOMENON_FOUND(8, "DCR.NoPhenomenonFound", "No Phenomenon with id {0}",Level.SEVERE),
    NO_CHANNEL_SPEC_FOUND(9, "DCR.NoChannelSpecFound", "No channel specification with id {0}",Level.SEVERE);

    public static final String COMPONENT_NAME = "DCR";

    private final int number;
    private final String key;
    private final String format;
    private final Level level;

    private MessageSeeds(int number, String key, String format, Level level) {
        this.number = number;
        this.key = key;
        this.format = format;
        this.level = level;
    }

    @Override
    public String getModule() {
        return COMPONENT_NAME;
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
