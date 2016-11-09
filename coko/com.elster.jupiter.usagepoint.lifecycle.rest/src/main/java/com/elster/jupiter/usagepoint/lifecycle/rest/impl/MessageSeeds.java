package com.elster.jupiter.usagepoint.lifecycle.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    NO_SUCH_LIFE_CYCLE(1, "no.such.life.cycle", "No life cycle with id {0}"),
    NO_SUCH_LIFE_CYCLE_STATE(2, "no.such.life.cycle.state", "No usage point state with id {0}"),
    NO_SUCH_BUSINESS_PROCESS(3, "no.such.state.process", "No business process with id {0}"),
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
