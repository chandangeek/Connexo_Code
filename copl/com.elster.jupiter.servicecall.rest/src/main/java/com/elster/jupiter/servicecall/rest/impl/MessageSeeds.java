/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    NO_SUCH_SERVICE_CALL(1, "NoSuchServiceCall", "Service call does not exist"),
    NO_SUCH_SERVICE_CALL_TYPE(2, "NoSuchServiceCallType", "Service call type does not exist"),
    NO_SUCH_SERVICE_CALL_LIFE_CYCLE(3, "NoSuchServiceCallLifeCycle", "Service call life cycle does not exist"),
    NO_SUCH_CUSTOM_ATTRIBUTE_SET(4, "NoSuchCustomAttributeSet", "Custom attribute set does not exist");

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
        return ServiceCallApplication.COMPONENT_NAME;
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

}
