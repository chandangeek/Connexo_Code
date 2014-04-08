package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    INVALID_VALUE(1, "DCR.InvalidValue", "Invalid value", Level.SEVERE),
    PROTOCOL_INVALID_NAME(2,"DCR.deviceType.no.such.protocol", "A protocol with name {0} does not exist",Level.SEVERE);

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
