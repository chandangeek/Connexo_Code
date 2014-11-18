package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed {

    NO_SUCH_READINGTYPE(1001, Keys.NO_SUCH_READINGTYPE, "Reading type {0} does not exist.", Level.SEVERE),
    FIELD_CAN_NOT_BE_EMPTY(1002, Keys.FIELD_CAN_NOT_BE_EMPTY, "Field can't be empty", Level.SEVERE),
    FIELD_SIZE_BETWEEN_1_AND_NAME_LENGTH(1003, Keys.FIELD_SIZE_BETWEEN_1_AND_NAME_LENGTH, "Field's text length should be between 1 and " + Table.NAME_LENGTH + " symbols", Level.SEVERE),
    NO_SUCH_PROCESSOR(1004, Keys.NO_SUCH_PROCESSOR, "Processor {0} does not exist.", Level.SEVERE),
    NAME_MUST_BE_UNIQUE(7, Keys.NAME_MUST_BE_UNIQUE, "Data export task with such name already exists", Level.SEVERE),;

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
        return DataExportService.COMPONENTNAME;
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

    public enum Keys {
        ;
        public static final String NO_SUCH_READINGTYPE = "NoSuchReadingType";
        public static final String FIELD_CAN_NOT_BE_EMPTY = "FieldCanNotBeEmpty";
        public static final String FIELD_SIZE_BETWEEN_1_AND_NAME_LENGTH = "FieldSizeBetween1and80";
        public static final String NO_SUCH_PROCESSOR = "NoSuchProcessor";
        public static final String NAME_MUST_BE_UNIQUE = "NameMustBeUnique";
    }


}

