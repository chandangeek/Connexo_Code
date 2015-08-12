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
    NO_SUCH_FORMATTER(1004, Keys.NO_SUCH_FORMATTER, "Formatter {0} does not exist.", Level.SEVERE),
    NAME_MUST_BE_UNIQUE(1005, Keys.NAME_MUST_BE_UNIQUE, "Data export task with such name already exists", Level.SEVERE),
    ITEM_FAILED(1006, "dataexport.item.failed", "Item {0}:{1} failed to export", Level.WARNING),
    ITEM_FATALLY_FAILED(1007, "dataexport.item.fatally.failed", "Item {0}:{1} fatally failed to export", Level.SEVERE),
    ITEM_EXPORTED_SUCCESFULLY(1008, "dataexport.item.success", "Item {0}:{1} exported succesfully for period {2} - {3}", Level.INFO),
    CANNOT_DELETE_WHILE_RUNNING(1009, "dataexport.cannot.delete", "Cannot delete a data export task (id = {0}) while it is running.", Level.SEVERE),
    RELATIVE_PERIOD_USED(1010, "dataexport.using.relativeperiod", "Relative period is still in use by the following data export tasks: {0}", Level.SEVERE),
    VETO_DEVICEGROUP_DELETION(1013, "deviceGroupXstillInUseByTask", "Device group {0} is still in use by an export task", Level.SEVERE),
    MUST_SELECT_READING_TYPE(1012, Keys.MUST_SELECT_AT_LEAST_ONE_READING_TYPE, "At least one reading type has to be selected", Level.SEVERE),
    NO_SUCH_SELECTOR(1014, Keys.NO_SUCH_SELECTOR, "Selector {0} does not exist", Level.SEVERE),
    FILE_IO(1015, "file.io.writing.failure", "Failure while doing IO on file {0} : {1}", Level.SEVERE),
    PARENT_BREAKING_PATH_NOT_ALLOWED(1016, Keys.PARENT_BREAKING_PATH, "Paths that navigate above parent are not allowed here", Level.SEVERE),
    INVALIDCHARS_EXCEPTION(1017, Keys.INVALIDCHARS_EXCEPTION, "Characters {0} are not allowed.", Level.SEVERE);

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
        public static final String MUST_SELECT_AT_LEAST_ONE_READING_TYPE = "MustHaveReadingTypes";
        public static final String FIELD_SIZE_BETWEEN_1_AND_NAME_LENGTH = "FieldSizeBetween1and80";
        public static final String NO_SUCH_FORMATTER = "NoSuchFormatter";
        public static final String NAME_MUST_BE_UNIQUE = "NameMustBeUnique";
        public static final String NO_SUCH_SELECTOR = "NoSuchSelector";
        public static final String PARENT_BREAKING_PATH = "path.parent.breaking.disallowed";
        public static final String INVALIDCHARS_EXCEPTION = "InvalidChars";
    }


}

