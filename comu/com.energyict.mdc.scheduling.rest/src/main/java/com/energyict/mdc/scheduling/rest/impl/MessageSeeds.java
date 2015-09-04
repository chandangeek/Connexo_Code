package com.energyict.mdc.scheduling.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    INVALID_VALUE(1, "InvalidValue", "Invalid value"),
    CAN_NOT_BE_EMPTY(2, "CanNotBeEmpty", "Field can not be empty"),
    ;

    public static final String COMPONENT_NAME = "SCR";

    private final int number;
    private final String key;
    private final String format;
    private final Level level;

    MessageSeeds(int number, String key, String format) {
        this.number = number;
        this.key = key;
        this.format = format;
        this.level = Level.SEVERE;
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
