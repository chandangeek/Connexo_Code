/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed {


    NOT_UNIQUE(1, Constants.NOT_UNIQUE, "Must be unique", Level.SEVERE),
    CAN_NOT_BE_EMPTY(2, Constants.NAME_REQUIRED_KEY, "This field is required", Level.SEVERE),
    FIELD_SIZE_BETWEEN_1_AND_80(6, Constants.FIELD_SIZE_BETWEEN_1_AND_80, "This field''s text length should be between 1 and 80 symbols", Level.SEVERE);


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
        return TaskService.COMPONENTNAME;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    public void log(Logger logger, Thesaurus thesaurus, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        logger.log(getLevel(), format.format(args));
    }

    public void log(Logger logger, Thesaurus thesaurus, Throwable t, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        logger.log(getLevel(), format.format(args), t);
    }

    public enum Constants {

        ;
        public static final String NOT_UNIQUE = "NotUnique";
        public static final String NAME_REQUIRED_KEY = "CanNotBeEmpty";
        public static final String FIELD_SIZE_BETWEEN_1_AND_80 = "FieldSizeBetween1and80";
    }


    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }
}

