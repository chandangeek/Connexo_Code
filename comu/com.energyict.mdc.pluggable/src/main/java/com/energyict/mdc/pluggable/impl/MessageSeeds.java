package com.energyict.mdc.pluggable.impl;

import com.energyict.mdc.pluggable.PluggableService;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (15:29)
 */
public enum MessageSeeds implements MessageSeed {
    NAME_IS_REQUIRED(1001, "pluggableClass.name.required", "The name of a pluggable class is required"),
    JAVA_CLASS_NAME_IS_REQUIRED(1002, "pluggableClass.javaName.required", "The java class name of a pluggable class \"{0}\" is required"),
    ALREADY_EXISTS(1003, "pluggableClass.duplicateNameX", "A pluggable class with name \"{0}\" already exists"),
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

    @Override
    public String getModule() {
        return PluggableService.COMPONENTNAME;
    }

}