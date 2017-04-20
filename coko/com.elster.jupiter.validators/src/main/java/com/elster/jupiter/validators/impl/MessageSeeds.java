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
    FAILED_VALIDATION_ON_PERIOD(1003, "validation.failed.on.period", "Failed to validate period {0} using method \"{1}\" on {2} since {3}"),

    MAIN_CHECK_MISC_NO_UP(1004, "maincheck.validator.misc.no.up", "Failed to validate period {0} using method \"{1}\" on {2} since main channel has no usage point"),
    MAIN_CHECK_MISC_NOT_ONE_EMC(1005, "maincheck.validator.misc.not.one.emc", "Failed to validate period {0} using method \"{1}\" on {2} since usage point must have one effective metrology configuration, but has {3]"),
    MAIN_CHECK_MISC_NO_PURPOSE(1006, "maincheck.validator.misc.no.purpose", "Failed to validate period {0} using method \"{1}\" on {2} since the specified purpose doesn't exist on the {4}"),
    MAIN_CHECK_MISC_PURPOSE_NEVER_ACTIVATED(1007, "maincheck.validator.misc.purpose.never.activated", "Failed to validate period {0} using method \"{1}\" on {2} since the specified purpose is never activated on effective metrology configuration on the {4}"),
    MAIN_CHECK_MISC_NO_CHECK_OUTPUT(1008, "maincheck.validator.misc.no.check.output", "Failed to validate period {0} using method \"{1}\" on {2} since 'check' output with matching reading type on the specified purpose doesn't exist on {3}"),
    MAIN_CHECK_MISC_CHECK_OUTPUT_MISSING_OR_NOT_VALID(1003, "maincheck.validator.misc.check.output.missing.or.not.valid", "Failed to validate period {0} using method \"{1}\" on {2}/{3} since data from 'check' output is missing or not validated");

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
