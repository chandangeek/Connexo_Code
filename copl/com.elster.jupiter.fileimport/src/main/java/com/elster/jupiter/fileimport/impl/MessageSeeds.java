package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed {
    FILE_IO(1001, "file.io.reading.failure", "Failure while doing IO on file {0}", Level.SEVERE),
    FAILED_TO_START_IMPORT_SCHEDULES(2001, "importschedule.start.failed", "Could not start Import schedules, please check if FIM is installed properly.", Level.SEVERE),
    NO_SUCH_IMPORTER(2002, "importschedule.noSuchImporter", "Importer {0} does not exist.", Level.SEVERE),
    CAN_NOT_BE_EMPTY(2, Constants.NAME_REQUIRED_KEY, "This field is required", Level.SEVERE),
    DUPLICATE_IMPORT_SCHEDULE(2, Constants.DUPLICATE_IMPORT_SCHEDULE, "Duplicate name", Level.SEVERE),
    FIELD_SIZE_BETWEEN_1_AND_256(4, Constants.FIELD_SIZE_BETWEEN_1_AND_4000, "Field's text length should be between 1 and 4000 symbols", Level.SEVERE),
    MISSING_PROPERTY(1002, "property.missing", "Required property with key ''{0}'' was not found.", Level.SEVERE),
    INVALID_CHARS(5, Constants.INVALID_CHARS, "This field contains invalid characters", Level.SEVERE),
    IMPORT_SCHEDULE_PROPERTY_NOT_IN_SPEC(1005, Constants.IMPORT_SCHEDULE_PROPERTY_NOT_IN_SPEC_KEY, "The import schedule ''{0}'' does not contain a specification for attribute ''{1}''", Level.SEVERE),
    IMPORT_SCHEDULE_PROPERTY_INVALID_VALUE(1006, Constants.IMPORT_SCHEDULE_PROPERTY_INVALID_VALUE_KEY, "This property contains an invalid value", Level.SEVERE),
    IMPORT_SCHEDULE_REQUIRED_PROPERTY_MISSING(1007, Constants.IMPORT_SCHEDULE_REQUIRED_PROPERTY_MISSING_KEY, "This field is required", Level.SEVERE),
    FILE_IMPORT_STARTED(1008, Constants.FILE_IMPORT_STARTED, "Start importing file", Level.INFO),
    FILE_IMPORT_FINISHED(1008, Constants.FILE_IMPORT_FINISHED, "Finish importing file", Level.INFO)
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
        return FileImportService.COMPONENT_NAME;
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
        public static final String NAME_REQUIRED_KEY = "CanNotBeEmpty";
        public static final String DUPLICATE_IMPORT_SCHEDULE = "DuplicateImportSchedule";
        public static final String INVALID_CHARS = "InvalidChars";
        public static final String FIELD_SIZE_BETWEEN_1_AND_80 = "FieldSizeBetween1and80";
        public static final String FIELD_SIZE_BETWEEN_1_AND_4000 = "FieldSizeBetween1and4000";
        public static final String IMPORT_SCHEDULE_PROPERTY_NOT_IN_SPEC_KEY = "ImportSchedulePropertyXIsNotInSpec";
        public static final String IMPORT_SCHEDULE_PROPERTY_INVALID_VALUE_KEY = "ImportSchedulerPropertyValueInvalid";
        public static final String IMPORT_SCHEDULE_REQUIRED_PROPERTY_MISSING_KEY = "ImportSchedulePropertyRequired";
        public static final String FILE_IMPORT_STARTED = "StartImportingFile";
        public static final String FILE_IMPORT_FINISHED = "FinishImportingFile";
    }

}
