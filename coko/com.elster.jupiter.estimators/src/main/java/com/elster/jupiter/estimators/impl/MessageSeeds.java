package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed {
    NO_SUCH_ESTIMATOR(1001, "estimator.doesnotexist", "Estimator {0} does not exist."),
    MISSING_PROPERTY(1002, "property.missing", "Required property with key ''{0}'' was not found."),
    INVALID_NUMBER_OF_SAMPLES(1003, "property.error.maxNumberOfSamples", "Maximum cannot be smaller than minimum"),
    INVALID_NUMBER_OF_CONSECUTIVE_SUSPECTS(1004, "property.error.invalidMaxNumberOfConsecutiveSuspects", "This value should be positive"),
    INVALID_ADVANCE_READINGTYPE(1005, "property.error.advanceReadingsSettings", "The reading type should be cumulative"),
    INVALID_NUMBER_OF_CONSECUTIVE_SUSPECTS_SHOULD_BE_INTEGER_VALUE(1006, "property.error.maxNumberOfConsecutiveSuspectsShouldBeInteger", "This value should be positive"),
    INVALID_ADVANCE_READINGTYPE_NONE_NOT_ALLOWED(1007, "property.error.advanceReadingsSettings.not.none", "None is not allowed");

    public static final String COMPONENT_NAME = "ESR";

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
