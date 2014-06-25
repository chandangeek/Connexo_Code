package com.elster.jupiter.validation.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.ValidationService;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed {

    CAN_NOT_BE_EMPTY(2, Constants.NAME_REQUIRED_KEY, "This field is required", Level.SEVERE),
    INVALID_CHARS(5, Constants.INVALID_CHARS, "This field contains invalid chars, should obey {regex}", Level.SEVERE),

    FIELD_SIZE_BETWEEN_1_AND_80(6, Constants.FIELD_SIZE_BETWEEN_1_AND_80, "Field's text length should be between 1 and 80 symbols", Level.SEVERE),
    FIELD_SIZE_BETWEEN_1_AND_256(7, Constants.FIELD_SIZE_BETWEEN_1_AND_256, "Field's text length should be between 1 and 256 symbols", Level.SEVERE),

    DUPLICATE_VALIDATION_RULE_SET(101, Constants.DUPLICATE_VALIDATION_RULE_SET, "Validation rule set with such name already exists", Level.SEVERE),
    DUPLICATE_VALIDATION_RULE(102, Constants.DUPLICATE_VALIDATION_RULE, "Validation rule with such name already exists", Level.SEVERE),

    NO_SUCH_VALIDATOR(1001, Constants.NO_SUCH_VALIDATOR, "Validator {0} does not exist.", Level.SEVERE),
    NO_SUCH_READINGTYPE(1002, Constants.NO_SUCH_READINGTYPE, "Reading type {0} does not exist.", Level.SEVERE);


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
        if (key.startsWith(getModule() + ".")){
            // +1 to skip the dot symbol
            return key.substring(getModule().length() + 1);
        }
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

final class Constants {

    public static final String DUPLICATE_VALIDATION_RULE_SET = "VAL.DuplicateValidationRuleSet";
    public static final String NAME_REQUIRED_KEY = "VAL.CanNotBeEmpty";
    public static final String INVALID_CHARS = "VAL.InvalidChars";
    public static final String NO_SUCH_VALIDATOR = "VAL.NoSuchValidator";
    public static final String DUPLICATE_VALIDATION_RULE = "VAL.DuplicateValidationRule";
    public static final String NO_SUCH_READINGTYPE = "VAL.NoSuchReadingType";
    public static final String FIELD_SIZE_BETWEEN_1_AND_80 = "VAL.FieldSizeBetween1and80";
    public static final String FIELD_SIZE_BETWEEN_1_AND_256 = "VAL.FieldSizeBetween1and256";


}
