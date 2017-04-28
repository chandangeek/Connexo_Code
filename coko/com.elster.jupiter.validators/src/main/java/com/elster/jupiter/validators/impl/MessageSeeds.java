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
    UNSUPPORTED_IRREGULAR_CHANNEL(1004, "UnsupportedIrregularChannel", "Irregular channels can''t be validated by ''{0}'' validator"),
    UNSUPPORTED_READINGTYPE(1005, "UnsupportedReadingType", "Channels of reading type ''{0}'' can''t be validated by ''{1}'' validator"),

    // common message for validators if there is no usage point on validating channel
    VALIDATOR_INIT_MISC_NO_UP(1006, "validator.init.misc.no.up", "Failed to validate period {0} using method \"{1}\" on {2} since validated channel has no usage point"),

    // common message for validators if there are not one effective metrology configuration on usage point
    VALIDATOR_MISC_NOT_ONE_EMC(1007, "validator.validator.misc.not.one.emc", "Failed to validate period {0} using method \"{1}\" on {2} since usage point must have one effective metrology configuration, but has {3}."),

    // Consecutive zeros messages
    MAX_PERIOD_SHORTER_THEN_MIN_PERIOD(1008, "maximum.period.less.minimum", "Maximum period less than minimum"),

    // FIXME: verify messages
    // Main/check validator messages
    MAIN_CHECK_MISC_NO_PURPOSE(2001, "reference.validator.misc.no.purpose", "Failed to validate period {0} using method \"{1}\" on {2} since the specified purpose doesn''t exist on the {3}"),
    MAIN_CHECK_MISC_PURPOSE_NEVER_ACTIVATED(2002, "reference.validator.misc.purpose.never.activated", "Failed to validate period {0} using method \"{1}\" on {2} since the specified purpose is never activated on effective metrology configuration on the {4}"),
    MAIN_CHECK_MISC_NO_CHECK_OUTPUT(2003, "reference.validator.misc.no.check.output", "Failed to validate period {0} using method \"{1}\" on {2} since 'check' output with matching reading type on the specified purpose doesn''t exist on {3}"),
    MAIN_CHECK_MISC_CHECK_OUTPUT_MISSING_OR_NOT_VALID(2004, "reference.validator.misc.check.output.missing.or.not.valid", "Failed to validate period {0} using method \"{1}\" on {2}/{3} since data from 'check' output is missing or not validated"),


    // FIXME: verify messages
    //Referece comparison validator messages
    // {0} - period,{1] - validator display name,{2} - validating usage point name,{3} - validating,{4} -,{5}-
    REFERENCE_MISC_NO_REFERENCE_USAGE_POINT(3001, "reference.validator.misc.no.reference.usagepoint", "Failed to validate period {0} using method \"{1}\" on {2}/{3}/{4} since the specified purpose/reading type doesn''t exist on the {5}"),
    REFERENCE_MISC_NO_PURPOSE(3002, "reference.validator.misc.no.purpose", "Failed to validate period {0} using method \"{1}\" on {2} since the specified purpose doesn''t exist on the {3}"),
    REFERENCE_MISC_PURPOSE_NEVER_ACTIVATED(3003, "reference.validator.misc.purpose.never.activated", "Failed to validate period {0} using method \"{1}\" on {2} since the specified purpose is never activated on effective metrology configuration on the {4}"),
    REFERENCE_MISC_NO_CHECK_OUTPUT(3004, "reference.validator.misc.no.check.output", "Failed to validate period {0} using method \"{1}\" on {2} since 'check' output with matching reading type on the specified purpose doesn''t exist on {3}"),
    REFERENCE_MISC_CHECK_OUTPUT_MISSING_OR_NOT_VALID(3005, "reference.validator.misc.check.output.missing.or.not.valid", "Failed to validate period {0} using method \"{1}\" on {2}/{3} since data from 'check' output is missing or not validated"),
    REFERENCE_MISC_REFERENCE_READING_TYPE_NOT_SUTABLE(3006, "reference.validator.misc.check.readingtype.not.sutable", "not sutable FIXME" ),
    REFERENCE_MISC_CONFIGURATION_NOT_COMPLETE(3007, "reference.validator.misc.not.complete","Failed to validate period {0} using method \"{1}\" on {2}/{3}/{4} since validation rule is not fully configured on {5}"),

    REFERENCE_VALIDATE_PROPS_NO_PURPOSE_ON_USAGE_POINT(3008,"reference.validator.validate.props.no.purpose","Purpose not found on usage point"),
    REFERENCE_VALIDATE_PROPS_NO_READING_TYPE_ON_PURPOSE_ON_USAGE_POINT(3009,"reference.validator.validate.props.no.readingtype","Reading type not found on purpose"),

    // Meter advance messages
    // {0} - from time, {1} - to time, {2} - validator display name, {3} - reading type name, {4} - validate object name
    REFERENCE_READINGTYPE_DOES_NOT_MATCH_VALIDATED_ONE(4001, "ReferenceReadingTypeDoesNotMatch",
            "Failed to validate period ''{0} until {1}'' using method ''{2}'' on ''{3}'' since the specified reference reading type doesn''t match the reading type on the {4}", Level.WARNING),
    NO_REFERENCE_READINGTYPE(4002, "NoReferenceReadingType",
            "Failed to validate period ''{0} until {1}'' using method ''{2}'' on ''{3}'' since the specified reference reading type doesn''t exist on the {4}", Level.WARNING),
    REGISTER_READINGS_ARE_MISSING(4003, "RegisterReadingsAreMissing",
            "Failed to validate period ''{0} until {1}'' using method ''{2}'' on ''{3}'' since register readings for the validated interval don''t exist on the {4}", Level.WARNING),
    VALIDATED_PERIOD_IS_GREATER_THAN_REFERENCE_PERIOD(4004, "ValidatedPeriodIsGreaterThanReferencePeriod",
            "The period ''{0} until {1}'' was marked valid using method ''{2}'' on ''{3}'' since the validated period length is greater than configured reference period", Level.INFO),
    DIFFERENCE_BETWEEN_TWO_REGISTER_READINGS_LESS_THAN_MIN_THRESHOLD(4005, "DiffLessThanMinThreshold",
            "The period ''{0} until {1}'' was marked valid using method ''{2}'' on ''{3}'' since the difference between the register readings is below the minimum threshold on {4}", Level.INFO),
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
