/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    NO_SUCH_LIFE_CYCLE(1, "no.such.life.cycle", "No life cycle with id {0}"),
    NO_SUCH_LIFE_CYCLE_STATE(2, "no.such.life.cycle.state", "No usage point state with id {0}"),
    NO_SUCH_BUSINESS_PROCESS(3, "no.such.state.process", "No business process with id {0}"),
    NO_SUCH_LIFE_CYCLE_TRANSITION(4, "no.such.life.cycle.transition", "No usage point transition with id {0}"),
    FIELD_CAN_NOT_BE_EMPTY(5, "field.can.not.be.empty", "This field is required"),
    NO_SUCH_USAGE_POINT(6, "no.such.usage.point", "No usage point with name {0}"),
    MISSING_REQUIRED_PROPERTY_VALUES(10, "transition.microAction.required.properties.missing", "No value was specified for the following property spec of the configured actions: {0}"),
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = Level.SEVERE;
    }

    @Override
    public String getModule() {
        return UsagePointLifeCycleApplication.COMPONENT_NAME;
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
        return this.level;
    }
}
