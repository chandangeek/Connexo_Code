/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    FIELD_TOO_LONG(1, Keys.FIELD_TOO_LONG, "Field must not exceed {max} characters"),
    CANNOT_BE_EMPTY(2, Keys.CAN_NOT_BE_EMPTY, "This field is required"),
    UNIQUE_USAGE_POINT_LIFE_CYCLE_NAME(3, Keys.UNIQUE_USAGE_POINT_LIFE_CYCLE_NAME, "Name must be unique"),
    CAN_NOT_REMOVE_STATE_HAS_TRANSITIONS(4, Keys.CAN_NOT_REMOVE_STATE_HAS_TRANSITIONS, "This state cannot be removed from this usage point life cycle because it is used on transitions: {0}."),
    CAN_NOT_REMOVE_LAST_STATE(5, Keys.CAN_NOT_REMOVE_LAST_STATE, "This state cannot be removed from this usage point life cycle because it is the latest state. Add another state first."),
    CAN_NOT_REMOVE_INITIAL_STATE(6, Keys.CAN_NOT_REMOVE_INITIAL_STATE, "This state cannot be removed from this usage point life cycle because it is the initial state. Set another state as initial state first."),
    TRANSITION_COMBINATION_OF_FROM_AND_NAME_NOT_UNIQUE(7, Keys.TRANSITION_COMBINATION_OF_FROM_AND_NAME_NOT_UNIQUE, "The combination 'Name' and 'From' has to be unique in the usage point life cycle."),
    TRANSITION_FROM_AND_TO_ARE_THE_SAME(8, Keys.TRANSITION_FROM_AND_TO_ARE_THE_SAME, "The 'From' and 'To' states must be different."),
    CAN_NOT_REMOVE_DEFAULT_LIFE_CYCLE(9, Keys.CAN_NOT_REMOVE_DEFAULT_LIFE_CYCLE, "'Default life cycle can''t be removed."),;

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
        public static final String CAN_NOT_REMOVE_DEFAULT_LIFE_CYCLE = "can.not.remove.default.life.cycle";
    }
}
