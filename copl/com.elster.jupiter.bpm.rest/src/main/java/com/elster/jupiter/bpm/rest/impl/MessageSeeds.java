package com.elster.jupiter.bpm.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    FIELD_CAN_NOT_BE_EMPTY(1, "BPM.FieldCanNotBeEmpty", "This field is required", Level.SEVERE);

    public static final String COMPONENT_NAME = "BPM";

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

