package com.elster.jupiter.usagepoint.lifecycle.execution.impl;

import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    FIELD_TOO_LONG(1, Keys.FIELD_TOO_LONG, "Field must not exceed {max} characters"),;

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
        public static final String CAN_NOT_BE_EMPTY = "CanNotBeEmpty";
        public static final String UNIQUE_USAGE_POINT_LIFE_CYCLE_NAME = "usage.point.life.cycle.unique.name";
        public static final String CAN_NOT_REMOVE_STATE_HAS_TRANSITIONS = "can.not.remove.state.has.transitions";
        public static final String CAN_NOT_REMOVE_LAST_STATE = "can.not.remove.last.state";
        public static final String CAN_NOT_REMOVE_INITIAL_STATE = "can.not.remove.initial.state";
        public static final String TRANSITION_COMBINATION_OF_FROM_AND_NAME_NOT_UNIQUE = "transition.combination.of.from.and.name.not.unique";
        public static final String TRANSITION_FROM_AND_TO_ARE_THE_SAME = "transition.from.and.to.are.the.same";
    }
}
