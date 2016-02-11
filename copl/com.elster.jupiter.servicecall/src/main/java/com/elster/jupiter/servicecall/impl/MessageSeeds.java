package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

/**
 * Created by bvn on 2/4/16.
 */
public enum MessageSeeds implements MessageSeed {
    BLABLA(1, "Blabla");

    private final int number;
    private final String defaultFormat;

    MessageSeeds(int number, String defaultFormat) {
        this.number = number;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getModule() {
        return ServiceCallService.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return name();
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }
}
