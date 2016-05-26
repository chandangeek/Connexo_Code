package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    SERVICE_CATEGORY_NOT_FOUND(1, "service.category.not.found", "Service category not found"),
    DEFAULT_METROLOGY_PURPOSE_NOT_FOUND(2, "default.metrology.purpose.not.found", "Default metrology purpose not found"),
    DEFAULT_METER_ROLE_NOT_FOUND(3, "default.meter.role.not.found", "Default meter role not found"),
    READING_TYPE_NOT_FOUND(4, "reading.type.not.found", "Reading type not found"),
    YOU_CANNOT_REMOVE_ACTIVE_METROLOGY_CONFIGURATION(5, "you.cannot.remove.active.metrology.configuration", "You can not remove active metrology configuration");

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
        return MeteringService.COMPONENTNAME;
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