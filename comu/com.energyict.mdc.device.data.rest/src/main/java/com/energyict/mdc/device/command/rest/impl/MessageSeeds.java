package com.energyict.mdc.device.command.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.rest.impl.DeviceApplication;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    MESSAGE_AUTHENTICATION_CHECK_FAILED_ONE_OR_MORE(1, "macCheckFailedOneOrMore", "Message authentication check on one or more command limitation rules failed. Please contact your system administrator."),
    MESSAGE_AUTHENTICATION_CHECK_FAILED_ONE(2, "macCheckFailedOne", "Message authentication check on the command limitation rule failed. Please contact your system administrator.");
    private final int number;
    private final String key;
    private final String format;

    MessageSeeds(int number, String key, String format) {
        this.number = number;
        this.key = key;
        this.format = format;
    }

    @Override
    public String getModule() {
        return DeviceApplication.COMPONENT_NAME;
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
        return Level.SEVERE;
    }

    public static class Keys {
        public static final String FIELD_CAN_NOT_BE_EMPTY = "FieldCanNotBeEmpty";
    }
}