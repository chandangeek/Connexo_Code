package com.energyict.mdc.device.alarms.rest.i18n;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

/**
 * Created by albertv on 11/29/2016.
 */
public enum MessageSeeds implements MessageSeed {

    NO_APPSERVER(1, "NoAppServer", "There is no active application server that can handle this request")
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat) {
        this(number, key, defaultFormat, Level.SEVERE);
    }

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    @Override
    public String getModule() {
        return "DAL";
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
