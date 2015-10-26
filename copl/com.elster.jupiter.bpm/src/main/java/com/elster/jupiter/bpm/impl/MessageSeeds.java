package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    NO_BPM_NAME_FOUND(1001, "bpm.name.not.found", "No BPM engine found with the name {0}.", Level.SEVERE);

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    @Override
    public String getModule() {
        return BpmService.COMPONENTNAME;
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
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return level;
    }
}
