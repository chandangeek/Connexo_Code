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

    ESTIMATOR_FAIL_NO_UP(1008,"estimator.fail.no.up","Failed to perform estimation using method {0} since usage point had not been found"),
    ESTIMATOR_FAIL_INTERNAL_ERROR(1009,"maincheck.estimator.fail.internal","Failed to estimate period \"{0}\" using method {1} on {2}/{3}/{4} due to internal error"),

    // {0} - period, {1} - estimator name, {2} - reading type, {3} - usage point
    MAINCHECK_ESTIMATOR_FAIL_EFFECTIVE_MC_NOT_FOUND(2002,"maincheck.estimator.fail.effective.mc.not.found","Failed to estimate period \"{0}\" using method {1} on {2} since effective metrology configuration has not been found on the {3}"),
    // {0} - period, {1} - estimator name, {2} - reading type, {3} - usage point
    MAINCHECK_ESTIMATOR_FAIL_PURPOSE_DOES_NOT_EXIST_ON_UP(2003,"maincheck.estimator.fail.purpose.does.no.exist","Failed to estimate period \"{0}\" using method {1} on {2} since the specified purpose doesn'\''t exist on the {3}"),
    // {0} - period, {1} - estimator name, {2} - reading type, {3} - usage point
    MAINCHECK_ESTIMATOR_FAIL_NO_OUTPUTS_ON_PURPOSE_WITH_READING_TYPE(2004,"maincheck.estimator.fail.no.output.on.purpose","Failed to estimate period \"{0}\" using method {1} on {2} since '\''check'\'' output with matching reading type on the specified purpose doesn'\''t exist on {3}"),
    // {0} - period, {1} - estimator name, {2} - usage point, {3} - purpose, {4} - reading type
    MAINCHECK_ESTIMATOR_FAIL_DATA_SUSPECT_OR_MISSING(2005,"maincheck.estimator.fail.data.suspect.or.missing","Failed to estimate period \"{0}\" using method {1} on {2}/{3}/{4} since data from 'check' output is suspect or missing"),
    // {0} - period, {1} - estimator name, {2} - usage point, {3} - purpose, {4} - reading type

    REFERENCE_ESTIMATOR_MISC_CONFIGURATION_NOT_COMPLETE(3001,"reference.estimator.misc.not.complete","Failed to validate period {0} using method \"{1}\" on {2}/{3}/{4} since the check usage point, purpose and reading type are not specified"),
    REFERENCE_VALIDATE_PROPS_NO_PURPOSE_ON_USAGE_POINT(3002,"reference.estimator.validate.props.no.purpose","Purpose not found on usage point"),
    REFERENCE_VALIDATE_PROPS_NO_READING_TYPE_ON_PURPOSE_ON_USAGE_POINT(3003,"reference.estimator.validate.props.no.readingtype","Reading type not found on purpose"),

    REFERENCE_ESTIMATOR_FAIL_EFFECTIVE_MC_NOT_FOUND(3004,"maincheck.estimator.fail.effective.mc.not.found","Failed to estimate period \"{0}\" using method {1} on {2} since effective metrology configuration has not been found on the {3}"),
    // {0} - period, {1} - estimator name, {2} - reading type, {3} - usage point
    REFERENCE_ESTIMATOR_FAIL_PURPOSE_DOES_NOT_EXIST_ON_UP(3005,"maincheck.estimator.fail.purpose.does.no.exist","Failed to estimate period \"{0}\" using method {1} on {2} since the specified purpose doesn'\''t exist on the {3}"),
    // {0} - period, {1} - estimator name, {2} - reading type, {3} - usage point
    REFERENCE_ESTIMATOR_FAIL_NO_OUTPUTS_ON_PURPOSE_WITH_READING_TYPE(3006,"maincheck.estimator.fail.no.output.on.purpose","Failed to estimate period \"{0}\" using method {1} on {2} since '\''check'\'' output with matching reading type on the specified purpose doesn'\''t exist on {3}"),
    // {0} - period, {1} - estimator name, {2} - usage point, {3} - purpose, {4} - reading type
    REFERENCE_ESTIMATOR_FAIL_DATA_SUSPECT_OR_MISSING(3007,"maincheck.estimator.fail.data.suspect.or.missing","Failed to estimate period \"{0}\" using method {1} on {2}/{3}/{4} since data from 'check' output is suspect or missing"),


    INVALID_NUMBER(1015,"property.error", "This value should be positive"),
    REQUIRED_FIELD(1016, "property.error", "This field is required"),
    // {0} - period, {1} - estimator name, {2} - reading type, {3} - usage point/meter
    NEAREST_AVG_VALUE_DAY_ESTIMATOR_FAIL_NOT_ENOUGH_SAMPLES(10017,"nearestavgvalueday.estimator.fail.not.enough.samples","Failed to estimate period ''{0}'' using method {1} due to insufficient (valid) number of sample values found on {2} / {3}"),
    NEAREST_AVG_VALUE_DAY_ESTIMATOR_FAIL_ESTIMATED_DAY_DISCARDED(10018,"nearestavgvalueday.estimator.fail.estimated.day.is.discarded","Failed to estimate period ''{0}'' using method {1} because the values to estimate belong to a day configured to be discarded on {2} / {3}");
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
