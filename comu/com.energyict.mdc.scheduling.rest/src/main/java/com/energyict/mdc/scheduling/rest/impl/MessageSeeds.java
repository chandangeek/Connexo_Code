package com.energyict.mdc.scheduling.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.scheduling.SchedulingService;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    INVALID_VALUE(1, "SCR.InvalidValue", "Invalid value"),
    CAN_NOT_BE_EMPTY(1, "SCR.CanNotBeEmpty", "Field can not be empty"),
    ;

    public static final String COMPONENT_NAME = "SCR";

    private final int number;
    private final String key;
    private final String format;
    private final Level level;

    private MessageSeeds(int number, String key, String format) {
        this.number = number;
        this.key = stripComponentNameIfPresent(key);
        this.format = format;
        this.level = Level.SEVERE;
    }

    private String stripComponentNameIfPresent(String key) {
        if (key.startsWith(SchedulingService.COMPONENT_NAME+".")) {
            return key.substring(SchedulingService.COMPONENT_NAME.length()+1);
        } else {
            return key;
        }
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
