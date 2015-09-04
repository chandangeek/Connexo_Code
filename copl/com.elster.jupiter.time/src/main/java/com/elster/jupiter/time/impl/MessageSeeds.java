package com.elster.jupiter.time.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed, TranslationKey {
    FIELD_CAN_NOT_BE_EMPTY(1, Keys.FIELD_CAN_NOT_BE_EMPTY, "Field can't be empty", Level.SEVERE),
    FIELD_SIZE_BETWEEN_1_AND_80(2, Keys.FIELD_SIZE_BETWEEN_1_AND_80, "Field's text length should be between 1 and 80 symbols", Level.SEVERE),

    NAME_MUST_BE_UNIQUE(7, Keys.NAME_MUST_BE_UNIQUE, "Relative period with such name already exists", Level.SEVERE),
    RELATIVE_PERIOD_IN_USE(8, Keys.RELATIVE_PERIOD_IN_USE, "{0} category(s) can't be deleted, relative period is in use.", Level.SEVERE),
    CATEGORY_MUST_BE_UNIQUE(9, Keys.CATEGORY_MUST_BE_UNIQUE, "Category is not unique", Level.SEVERE),
    UNKNOWN_TIME_UNIT (10, Keys.UNKNOWN_TIME_UNIT, "Unknown time unit ''{0}''", Level.SEVERE),
    INVALID_TIME_COUNT (11, Keys.INVALID_TIME_COUNT, "{0} is not a number", Level.SEVERE),
    ;

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
        return TimeService.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return this.number;
    }

    @Override
    public String getKey() {
        if (key.startsWith(getModule() + ".")) {
            // +1 to skip the dot symbol
            return key.substring(getModule().length() + 1);
        }
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

    public String getFormated(Object... args) {
        return MessageSeeds.getFormated(this, args);
    }

    public static String getFormated(MessageSeed messageSeed, Object... args) {
        return MessageFormat.format(messageSeed.getDefaultFormat(), args);
    }

    public void log(Logger logger, Thesaurus thesaurus, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        logger.log(getLevel(), format.format(args));
    }

    public void log(Logger logger, Thesaurus thesaurus, Throwable t, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        logger.log(getLevel(), format.format(args), t);
    }

    public static class Keys {
        private Keys() {
        }

        private static final String KEY_PREFIX = TimeService.COMPONENT_NAME + ".";

        public static final String FIELD_CAN_NOT_BE_EMPTY = "FieldCanNotBeEmpty";
        public static final String FIELD_SIZE_BETWEEN_1_AND_80 = "FieldSizeBetween1and80";
        public static final String NAME_MUST_BE_UNIQUE = "NameMustBeUnique";
        public static final String RELATIVE_PERIOD_IN_USE = "RelativePeriodInUse";
        public static final String CATEGORY_MUST_BE_UNIQUE = "CategoryMustBeUnique";
        public static final String UNKNOWN_TIME_UNIT = "UnknownTimeUnit";
        public static final String INVALID_TIME_COUNT = "InvalidTimeCount";
    }

}
