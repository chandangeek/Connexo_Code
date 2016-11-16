package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    FIELD_TOO_LONG(1, Keys.FIELD_TOO_LONG, "Field must not exceed {max} characters"),
    USER_CAN_NOT_PERFORM_TRANSITION(2, Keys.USER_CAN_NOT_PERFORM_TRANSITION, "The current user is not allowed to perform this transition");

    private final int number;
    private final String key;
    private final String defaultFormat;

    MessageSeeds(int number, String key, String defaultFormat) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getModule() {
        return UsagePointLifeCycleConfigurationService.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return this.number;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }

    static final class Keys {
        private Keys() {
        }

        public static final String FIELD_TOO_LONG = "FieldTooLong";
        public static final String USER_CAN_NOT_PERFORM_TRANSITION = "user.can.not.perform.transition";
    }
}
