/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed {
    NO_SUCH_VALIDATOR(1001, "validator.doesnotexist", "Validator {0} does not exist."),
    MISSING_PROPERTY(1002, "property.missing", "Required property with key ''{0}'' was not found."),

    // Meter advance messages
    NOT_APPLICABLE_TO_IRREGULAR_CHANNEL(2001, "UnsupportedReadingType", "Not supported reading type"),
    NOT_SUPPORTED_READINGTYPE(2001, "UnsupportedReadingType", "Not supported reading type"),
    REFERENCE_READINGTYPE_DOESNOT_MATCH_VALIDATED_ONE(2002, "", "reference reading type doesn't match the <reading type> reading type"),
    NO_REFERENCE_READINGTYPE(2003, "", "no reference reading type on usage point/device"),
    REGISTER_READINGS_ARE_MISSING(2004, "", "since register readings for the validated interval don't exist on <usage point name>/<device>"),
    DIFFERENCE_BETWEEN_TWO_REGISTER_READINGS_LESS_THEN_MIN_THRESHOLD(2005, "DiffLessThenMin", ""),
    ;

    public static final String COMPONENT_NAME = "VDR";

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
        return COMPONENT_NAME;
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

    public void log(Logger logger, Thesaurus thesaurus, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        logger.log(getLevel(), format.format(args));
    }

    public void log(Logger logger, Thesaurus thesaurus, Throwable t, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        logger.log(getLevel(), format.format(args), t);
    }

}
