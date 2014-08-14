package com.elster.jupiter.validation;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed {

    CAN_NOT_BE_EMPTY(2, Constants.NAME_REQUIRED_KEY, "This field is required", Level.SEVERE),
    INVALID_CHARS(5, Constants.INVALID_CHARS, "This field contains invalid chars, should obey {regex}", Level.SEVERE),

    FIELD_SIZE_BETWEEN_1_AND_80(6, Constants.FIELD_SIZE_BETWEEN_1_AND_80, "Field's text length should be between 1 and 80 symbols", Level.SEVERE),
    FIELD_SIZE_BETWEEN_1_AND_256(7, Constants.FIELD_SIZE_BETWEEN_1_AND_4000, "Field's text length should be between 1 and 4000 symbols", Level.SEVERE),

    DUPLICATE_VALIDATION_RULE_SET(101, Constants.DUPLICATE_VALIDATION_RULE_SET, "Validation rule set with such name already exists", Level.SEVERE),
    DUPLICATE_VALIDATION_RULE(102, Constants.DUPLICATE_VALIDATION_RULE, "Validation rule with such name already exists", Level.SEVERE),

    NO_SUCH_VALIDATOR(1001, Constants.NO_SUCH_VALIDATOR, "Validator {0} does not exist.", Level.SEVERE),
    NO_SUCH_READINGTYPE(1002, Constants.NO_SUCH_READINGTYPE, "Reading type {0} does not exist.", Level.SEVERE),

    VALIDATOR_PROPERTY_NOT_IN_SPEC(1005, Constants.VALIDATOR_PROPERTY_NOT_IN_SPEC_KEY, "The validator ''{0}'' does not contain a specification for attribute ''{1}''", Level.SEVERE),
    VALIDATOR_PROPERTY_INVALID_VALUE(1006, Constants.VALIDATOR_PROPERTY_INVALID_VALUE_KEY, "''{0}'' is not a valid value for attribute ''{1}'' of validator ''{2}''", Level.SEVERE),
    VALIDATOR_REQUIRED_PROPERTY_MISSING(1007, Constants.VALIDATOR_REQUIRED_PROPERTY_MISSING_KEY, "A value is missing for required attribute ''{0}'' of validator ''{1}''", Level.SEVERE);

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
        return ValidationService.COMPONENTNAME;
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
        public static final String DUPLICATE_VALIDATION_RULE_SET = "DuplicateValidationRuleSet";
        public static final String NAME_REQUIRED_KEY = "CanNotBeEmpty";
        public static final String INVALID_CHARS = "InvalidChars";
        public static final String NO_SUCH_VALIDATOR = "NoSuchValidator";
        public static final String DUPLICATE_VALIDATION_RULE = "DuplicateValidationRule";
        public static final String NO_SUCH_READINGTYPE = "NoSuchReadingType";
        public static final String FIELD_SIZE_BETWEEN_1_AND_80 = "FieldSizeBetween1and80";
        public static final String FIELD_SIZE_BETWEEN_1_AND_4000 = "FieldSizeBetween1and4000";
        public static final String VALIDATOR_PROPERTY_NOT_IN_SPEC_KEY = "ValidatorPropertyXIsNotInSpec";
        public static final String VALIDATOR_PROPERTY_INVALID_VALUE_KEY = "ValidatorPropertyValueInvalid";
        public static final String VALIDATOR_REQUIRED_PROPERTY_MISSING_KEY = "ValidatorPropertyRequired";
    }
}

