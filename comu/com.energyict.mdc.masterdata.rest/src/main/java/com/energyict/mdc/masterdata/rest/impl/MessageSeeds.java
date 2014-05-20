package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    NO_LOAD_PROFILE_TYPE_FOUND(1, "MDR.NoLoadProfileTypeFound", "No LoadProfile type with id {0}",Level.SEVERE),
    TIME_MINUTE(2, "MDR.TimeMinute", "%s minute",Level.SEVERE),
    TIME_MINUTES(3, "MDR.TimeMinutes", "%s minutes",Level.SEVERE),
    TIME_HOUR(4, "MDR.TimeHour", "%s hour",Level.SEVERE),
    TIME_DAY(5, "MDR.TimeDay", "%s day",Level.SEVERE),
    TIME_WEEK(6, "MDR.TimeWeek", "%s week",Level.SEVERE);

    private final int number;
    private final String key;
    private final String format;
    private final Level level;

    private MessageSeeds(int number, String key, String format, Level level) {
        this.number = number;
        this.key = key;
        this.format = format;
        this.level = level;
    }

    @Override
    public String getModule() {
        return MasterDataApplication.COMPONENT_NAME;
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
        return format;
    }

    @Override
    public Level getLevel() {
        return level;
    }


}
