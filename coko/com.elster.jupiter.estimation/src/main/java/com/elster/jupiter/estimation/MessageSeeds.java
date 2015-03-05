package com.elster.jupiter.estimation;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;
import java.util.logging.Logger;


public enum MessageSeeds implements MessageSeed {
    NO_SUCH_ESTIMATOR(1001, Constants.NO_SUCH_ESTIMATOR, "Validator {0} does not exist.", Level.SEVERE),
    MISSING_PROPERTY(1002, "property.missing", "Required property with key ''{0}'' was not found.", Level.SEVERE);

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
//        public static final String DUPLICATE_VALIDATION_RULE_SET = "DuplicateValidationRuleSet";
//        public static final String NAME_REQUIRED_KEY = "CanNotBeEmpty";
//        public static final String INVALID_CHARS = "InvalidChars";
        public static final String NO_SUCH_ESTIMATOR = "NoSuchEstimator";
//        public static final String DUPLICATE_VALIDATION_RULE = "DuplicateValidationRule";
//        public static final String NO_SUCH_READINGTYPE = "NoSuchReadingType";
//        public static final String FIELD_SIZE_BETWEEN_1_AND_80 = "FieldSizeBetween1and80";
//        public static final String FIELD_SIZE_BETWEEN_1_AND_4000 = "FieldSizeBetween1and4000";
//        public static final String VALIDATOR_PROPERTY_NOT_IN_SPEC_KEY = "ValidatorPropertyXIsNotInSpec";
//        public static final String VALIDATOR_PROPERTY_INVALID_VALUE_KEY = "ValidatorPropertyValueInvalid";
//        public static final String VALIDATOR_REQUIRED_PROPERTY_MISSING_KEY = "ValidatorPropertyRequired";
    }
}

