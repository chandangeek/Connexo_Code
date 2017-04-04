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

    BAD_MAIN_CHECK_CONFIGURATION(1003, "validator.maincheck.misc", "Main/check validation failed due to misconfiguration"),
    BAD_MAIN_CHECK_CONFIGURATION_NO_UP_ON_CHANNEL(1004, "validator.maincheck.misc.no.up.on.channel", "Main/check validation failed. Channel has no usage point"),
    BAD_MAIN_CHECK_CONFIGURATION_METRLOGY_CONFIG_COUNT(1005, "validator.maincheck.misc.metrology.config.count", "Main/check validation failed. Usage point has not one effective metrology configuration"),
    BAD_MAIN_CHECK_CONFIGURATION_METRLOGY_CONTRACT(1006, "validator.maincheck.misc.metrology.contract", "Main/check validation failed. No metrology contract has been found for check purpose"),
    BAD_MAIN_CHECK_CONFIGURATION_CHANNELS_CONTAINER(1007, "validator.maincheck.misc.channels.container", "Main/check validation failed. No channels container has been found for check purpose"),
    BAD_MAIN_CHECK_CONFIGURATION_CHECK_CHANNEL(1008, "validator.maincheck.misc.check.channel", "Main/check validation failed. No channel has been found for check purpose");

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
