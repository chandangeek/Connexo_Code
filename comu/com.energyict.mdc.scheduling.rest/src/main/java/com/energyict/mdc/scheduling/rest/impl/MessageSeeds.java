package com.energyict.mdc.scheduling.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    INVALID_VALUE(1, "SCR.InvalidValue", "Invalid value", Level.SEVERE);

    public static final String COMPONENT_NAME = "SCR";

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
