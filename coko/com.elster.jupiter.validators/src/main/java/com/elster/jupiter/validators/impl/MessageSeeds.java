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
    UNSUPPORTED_IRREGULAR_CHANNEL(1003, "UnsupportedIrregularChannel", "Irregular channels can''t be validated by ''{0}'' validator"),
    UNSUPPORTED_READINGTYPE(1004, "UnsupportedReadingType", "Channels of reading type ''{0}'' can''t be validated by ''{1}'' validator"),

    // Meter advance messages
    // {0} - from time, {1} - to time, {2} - validator display name, {3} - reading type mrid, {4} - validate object name
    REFERENCE_READINGTYPE_DOES_NOT_MATCH_VALIDATED_ONE(2002, "ReferenceReadingTypeDoesNotMatch",
            "Failed to validate period ''{0} until {1}'' using method ''{2}'' on {3} since the specified reference reading type doesn''t match the reading type on the {4}"),
    NO_REFERENCE_READINGTYPE(2003, "NoReferenceReadingType",
            "Failed to validate period ''{0} until {1}'' using method ''{2}'' on {3} since the specified reference reading type doesn''t exist on the {4}"),
    REGISTER_READINGS_ARE_MISSING(2004, "RegisterReadingsAreMissing",
            "Failed to validate period ''{0} until {1}'' using method ''{2}'' on {3} since register readings for the validated interval don''t exist on the {4}"),
    DIFFERENCE_BETWEEN_TWO_REGISTER_READINGS_LESS_THAN_MIN_THRESHOLD(2005, "DiffLessThanMinThreshold",
            "The period ''{0} until {1}'' was marked valid using method ''{2}'' on {3} since the difference between the register readings is below the minimum threshold on {4}", Level.WARNING),
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
