/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bootstrap;

import com.elster.jupiter.util.exception.MessageSeed;
import java.util.logging.Level;

enum MessageSeeds implements MessageSeed {
    PROPERTY_NOT_FOUND(1001, "Property with key \"{0}\" not found"),
    DATASOURCE_SETUP_FAILED(1002, "Data source setup failed.");

    private final int number;
    private final String defaultFormat;

    MessageSeeds(int number, String defaultFormat) {
        this.number = number;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getModule() {
        return "BTS";
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
