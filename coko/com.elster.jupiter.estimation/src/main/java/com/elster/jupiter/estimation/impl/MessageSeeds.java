package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed {
    NO_SUCH_ESTIMATOR(1001, Constants.NO_SUCH_ESTIMATOR, "Estimator {0} does not exist.", Level.SEVERE),
    MISSING_PROPERTY(1002, "property.missing", "Required property with key ''{0}'' was not found.", Level.SEVERE),

    CAN_NOT_BE_EMPTY(1, Constants.NAME_REQUIRED_KEY, "This field is required", Level.SEVERE),
    INVALID_CHARS(2, Constants.INVALID_CHARS, "This field contains invalid characters", Level.SEVERE),
    FIELD_SIZE_BETWEEN_1_AND_80(3, Constants.FIELD_SIZE_BETWEEN_1_AND_80, "Field's text length should be between 1 and 80 symbols", Level.SEVERE),
    FIELD_SIZE_BETWEEN_1_AND_256(4, Constants.FIELD_SIZE_BETWEEN_1_AND_4000, "Field's text length should be between 1 and 4000 symbols", Level.SEVERE),

    DUPLICATE_ESTIMATION_RULE_SET(101, Constants.DUPLICATE_ESTIMATION_RULE_SET, "Name must be unique", Level.SEVERE),
    DUPLICATE_ESTIMATION_RULE(102, Constants.DUPLICATE_ESTIMATION_RULE, "Name must be unique", Level.SEVERE),
    DUPLICATE_ESTIMATION_TASK(103, Constants.DUPLICATE_ESTIMATION_TASK, "Name must be unique", Level.SEVERE),

    ESTIMATOR_PROPERTY_NOT_IN_SPEC(1005, Constants.ESTIMATOR_PROPERTY_NOT_IN_SPEC_KEY, "The estimator ''{0}'' does not contain a specification for attribute ''{1}''", Level.SEVERE),
    ESTIMATOR_PROPERTY_INVALID_VALUE(1006, Constants.ESTIMATOR_PROPERTY_INVALID_VALUE_KEY, "''{0}'' is not a valid value for attribute ''{1}'' of estimator ''{2}''", Level.SEVERE),
    ESTIMATOR_REQUIRED_PROPERTY_MISSING(1007, Constants.ESTIMATOR_REQUIRED_PROPERTY_MISSING_KEY, "This field is required", Level.SEVERE),

    VETO_DEVICEGROUP_DELETION(1008, Constants.VETO_DEVICEGROUP_DELETION_KEY, "Device group {0} is still in use by an estimation task", Level.SEVERE);

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
        return EstimationService.COMPONENTNAME;
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

    public enum Constants {
        ;
        public static final String DUPLICATE_ESTIMATION_RULE_SET = "DuplicateEstimationRuleSet";
        public static final String NAME_REQUIRED_KEY = "CanNotBeEmpty";
        public static final String INVALID_CHARS = "InvalidChars";
        public static final String NO_SUCH_ESTIMATOR = "NoSuchEstimator";
        public static final String DUPLICATE_ESTIMATION_RULE = "DuplicateEstimationRule";
        public static final String DUPLICATE_ESTIMATION_TASK = "DuplicateEstimationTask";
        public static final String NO_SUCH_READINGTYPE = "NoSuchReadingType";
        public static final String FIELD_SIZE_BETWEEN_1_AND_80 = "FieldSizeBetween1and80";
        public static final String FIELD_SIZE_BETWEEN_1_AND_4000 = "FieldSizeBetween1and4000";
        public static final String ESTIMATOR_PROPERTY_NOT_IN_SPEC_KEY = "EstimatorPropertyXIsNotInSpec";
        public static final String ESTIMATOR_PROPERTY_INVALID_VALUE_KEY = "EstimatorPropertyValueInvalid";
        public static final String ESTIMATOR_REQUIRED_PROPERTY_MISSING_KEY = "EstimatorPropertyRequired";
        public static final String VETO_DEVICEGROUP_DELETION_KEY = "DeviceGroupXStillInUseByEstimationTask";
    }
}

