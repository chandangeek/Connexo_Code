package com.elster.jupiter.rest.util.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    INVALID_VALUE(1, "RUT.InvalidValue", "Invalid value", Level.SEVERE),
    FIELD_CAN_NOT_BE_EMPTY(2, "RUT.FieldCanNotBeEmpty", "This field is required", Level.SEVERE),
    FIELD_SHOULD_HAVE_BEEN_ARRAY(3, "RUT.ExpectedArray", "Expected list of values", Level.SEVERE),
    ;

    public static final String COMPONENT_NAME = "RUT";

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
