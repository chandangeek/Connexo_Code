/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

/**
 * Created by bvn on 5/3/16.
 */
public enum MessageSeeds implements MessageSeed {

    FIELD_REQUIRED(1, Keys.FIELD_REQUIRED, "This field is required", Level.SEVERE),
    FIELD_TOOL_LONG(2, Keys.FIELD_TOO_LONG, "This field is too long", Level.SEVERE),
    FIELD_NOT_UNIQUE(3, Keys.FIELD_MUST_BE_UNIQUE, "This field must be unique", Level.SEVERE),
    INVALID_FILE_NAME(4, Keys.INVALID_FILE_NAME, "Invalid file name", Level.SEVERE),
    INVALID_PATH(5, Keys.INVALID_PATH, "Invalid path", Level.SEVERE);

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
        return WebServicesService.COMPONENT_NAME;
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

    public static interface Keys {
        String FIELD_REQUIRED = "field.required";
        String FIELD_TOO_LONG = "ThisFieldIsTooLong";
        String FIELD_MUST_BE_UNIQUE = "FieldMustBeUnique";
        String INVALID_FILE_NAME = "InvalidFileName";
        String INVALID_PATH = "InvalidPath";
    }

}
