/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
    INVALID_PERIOD_OF_ZERO_OR_NEGATIVE_LENGTH(1004, "property.error.invalidMaxPeriodOfConsecutiveSuspects", "The period should have positive length"),
    INVALID_ADVANCE_READINGTYPE(1005, "property.error.advanceReadingsSettings", "The reading type should be cumulative"),
    INVALID_ADVANCE_READINGTYPE_NONE_NOT_ALLOWED(1007, "property.error.advanceReadingsSettings.not.none", "None is not allowed"),

    // {0} - estimator name,
    MAINCHECK_ESTIMATOR_FAIL_NO_UP(1008,"maincheck.estimator.fail.no.up","Failed to perform estimation using method {0} since usage point had not been found"),
    // {0} - period, {1} - estimator name, {2} - reading type, {3} - usage point
    MAINCHECK_ESTIMATOR_FAIL_EFFECTIVE_MC_NOT_FOUND(1009,"maincheck.estimator.fail.effective.mc.not.found","Failed to estimate period \"{0}\" using method {1} on {2} since effective metrology configuration has not been found on the {3}"),
    // {0} - period, {1} - estimator name, {2} - reading type, {3} - usage point
    MAINCHECK_ESTIMATOR_FAIL_PURPOSE_DOES_NOT_EXIST_ON_UP(1010,"maincheck.estimator.fail.purpose.does.no.exist","Failed to estimate period \"{0}\" using method {1} on {2} since the specified purpose doesn'\''t exist on the {3}"),
    // {0} - period, {1} - estimator name, {2} - reading type, {3} - usage point
    MAINCHECK_ESTIMATOR_FAIL_NO_OUTPUTS_ON_PURPOSE_WITH_READING_TYPE(1011,"maincheck.estimator.fail.no.output.on.purpose","Failed to estimate period \"{0}\" using method {1} on {2} since '\''check'\'' output with matching reading type on the specified purpose doesn'\''t exist on {3}"),
    // {0} - period, {1} - estimator name, {2} - usage point, {3} - purpose, {4} - reading type
    MAINCHECK_ESTIMATOR_FAIL_DATA_SUSPECT_OR_MISSING(1012,"maincheck.estimator.fail.data.suspect.or.missing","Failed to estimate period \"{0}\" using method {1} on {2}/{3}/{4} since data from 'check' output is suspect or missing"),
    // {0} - period, {1} - estimator name, {2} - usage point, {3} - purpose, {4} - reading type
    MAINCHECK_ESTIMATOR_FAIL_INTERNAL_ERROR(1013,"maincheck.estimator.fail.internal","Failed to estimate period \"{0}\" using method {1} on {2}/{3}/{4} due to internal error"),

    INVALID_NUMBER(1015,"property.error", "This value should be positive"),
    INVALID_DISCARD_DAY_FIELD(1016, "property.error.discardDaySettings.not.null", "This field is required");
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
