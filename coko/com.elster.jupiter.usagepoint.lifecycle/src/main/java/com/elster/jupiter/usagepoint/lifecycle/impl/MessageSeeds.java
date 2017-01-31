/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    FIELD_TOO_LONG(1, Keys.FIELD_TOO_LONG, "Field must not exceed {max} characters"),
    USER_CAN_NOT_PERFORM_TRANSITION(2, Keys.USER_CAN_NOT_PERFORM_TRANSITION, "The current user is not allowed to perform this transition."),
    TRANSITION_NOT_FOUND(3, Keys.TRANSITION_NOT_FOUND, "Transition with id {0} doesn''t exist."),
    USAGE_POINT_STATE_DOES_NOT_SUPPORT_TRANSITION(4, Keys.USAGE_POINT_STATE_DOES_NOT_SUPPORT_TRANSITION, "Usage point state ''{0}'' doesn''t support transition to state ''{1}''."),
    MICRO_CHECKS_FAILED_NO_PARAM(5, Keys.MICRO_CHECKS_FAILED_NO_PARAM, "Failed pre-transition checks:"),
    MICRO_CHECKS_FAILED(6, Keys.MICRO_CHECKS_FAILED, "Failed pre-transition checks: {0}"),
    MICRO_ACTION_FAILED_NO_PARAM(7, Keys.MICRO_ACTION_FAILED_NO_PARAM, "Failed auto actions:"),
    TRANSITION_DATE_MUST_BE_GREATER_THAN_LATEST_STATE_CHANGE(8, Keys.TRANSITION_DATE_MUST_BE_GREATER_THAN_LATEST_STATE_CHANGE, "The transition date must be greater than latest state modification date {0}"),
    TRANSITION_ALREADY_PLANNED_FOR_USAGE_POINT(9, Keys.TRANSITION_ALREADY_PLANNED_FOR_USAGE_POINT, "Another transition is already planned on {0}"),;

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
        public static final String TRANSITION_NOT_FOUND = "transition.not.found";
        public static final String USAGE_POINT_STATE_DOES_NOT_SUPPORT_TRANSITION = "usage.point.state.does.not.support.transition";
        public static final String MICRO_CHECKS_FAILED_NO_PARAM = "micro.checks.failed.no.param";
        public static final String MICRO_CHECKS_FAILED = "micro.checks.failed";
        public static final String MICRO_ACTION_FAILED_NO_PARAM = "micro.action.failed.no.param";
        public static final String TRANSITION_DATE_MUST_BE_GREATER_THAN_LATEST_STATE_CHANGE = "transition.date.must.be.greater.than.latest.state.change";
        public static final String TRANSITION_ALREADY_PLANNED_FOR_USAGE_POINT = "transition.already.planned.for.usage.point";
    }
}
