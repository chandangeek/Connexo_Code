package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    FIELD_TOO_LONG(1, Keys.FIELD_TOO_LONG, "Field must not exceed {max} characters"),
    CANNOT_BE_EMPTY(2, Keys.CAN_NOT_BE_EMPTY, "This field is required"),
    UNIQUE_USAGE_POINT_LIFE_CYCLE_NAME(3, Keys.UNIQUE_USAGE_POINT_LIFE_CYCLE_NAME, "Name must be unique"),
    CAN_NOT_REMOVE_STATE_HAS_TRANSITIONS(4, Keys.CAN_NOT_REMOVE_STATE_HAS_TRANSITIONS, "This state cannot be removed from this usage point life cycle because it is used on transitions: {0}"),
    CAN_NOT_REMOVE_LAST_STATE(5, Keys.CAN_NOT_REMOVE_LAST_STATE, "This state cannot be removed from this usage point life cycle because it is the latest state. Add another state first."),
    CAN_NOT_REMOVE_INITIAL_STATE(6, Keys.CAN_NOT_REMOVE_INITIAL_STATE, "This state cannot be removed from this usage point life cycle because it is the initial state. Set another state as initial state first."),;

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
        return UsagePointLifeCycleService.COMPONENT_NAME;
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
        public static final String CAN_NOT_BE_EMPTY = "CanNotBeEmpty";
        public static final String UNIQUE_USAGE_POINT_LIFE_CYCLE_NAME = "usage.point.life.cycle.unique.name";
        public static final String CAN_NOT_REMOVE_STATE_HAS_TRANSITIONS = "can.not.remove.state.has.transitions";
        public static final String CAN_NOT_REMOVE_LAST_STATE = "can.not.remove.last.state";
        public static final String CAN_NOT_REMOVE_INITIAL_STATE = "can.not.remove.initial.state";
    }
}
