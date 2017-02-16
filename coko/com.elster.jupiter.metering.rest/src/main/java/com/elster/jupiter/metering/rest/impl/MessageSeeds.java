/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    SERVICE_CATEGORY_NOT_FOUND(1, "service.category.not.found", "Service category not found"),
    DEFAULT_METROLOGY_PURPOSE_NOT_FOUND(2, "default.metrology.purpose.not.found", "Default metrology purpose not found"),
    DEFAULT_METER_ROLE_NOT_FOUND(3, "default.meter.role.not.found", "Default meter role not found"),
    READING_TYPE_NOT_FOUND(4, "reading.type.not.found", "Reading type not found"),
    YOU_CANNOT_REMOVE_ACTIVE_METROLOGY_CONFIGURATION(5, "you.cannot.remove.active.metrology.configuration", "You can not remove active metrology configuration"),
    METROLOGY_CONFIG_VERSION_CONCURRENCY_ERROR_ON_USAGE_POINT_TITLE(6, "failed.to.save.metrology.config.version.body", "Failed to save metrology configuration version on ''{0}''."),
    METROLOGY_CONFIG_VERSION_CONCURRENCY_ERROR_ON_USAGE_POINT_BODY(7, "failed.to.save.metrology.config.version.title", "Usage point ''{0}'' has been modified since the page was last updated."),
    NO_METROLOGY_CONFIG_VERSION_WITH_START(8, "no.metrology.config.found", "No metrology configuration version were found with start ''{0}''");


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
