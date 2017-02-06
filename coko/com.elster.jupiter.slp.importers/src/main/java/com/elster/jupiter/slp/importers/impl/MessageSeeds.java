/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.slp.importers.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed {
    DATE_FORMAT_IS_NOT_VALID(1, "DateFormatIsNotValid", "Invalid date format", Level.SEVERE),
    TIME_ZONE_IS_NOT_VALID(2, "TimeZoneIsNotValid", "Invalid time zone", Level.SEVERE),
    NUMBER_FORMAT_IS_INCOMPATIBLE_WITH_DELIMITER(3, "NumberFormatIncompatibleWithDelimiter", "Number format is incompatible with delimiter", Level.SEVERE),

    MISSING_TITLE_ERROR(4, "TitleMissingError", "File format error: wrong number of title columns in the first line. Importer service expects {0} but was {1}.", Level.SEVERE),
    FILE_FORMAT_ERROR(5, "FileFormatError", "File format error: wrong number of columns in the line {0}. Importer service expects {1} but was {2}.", Level.SEVERE),
    LINE_MISSING_VALUE_ERROR(6, "LineMissingValueError", "Format error for line {0}: missing mandatory value for column {1}.", Level.SEVERE),
    LINE_FORMAT_ERROR(7, "LineFormatError", "Format error for line {0}: wrong value format for column {1} (expected format = ''{2}'')", Level.SEVERE),

    CORRECTIONFACTOR_HEADER_NOT_FOUND(8, "NoSuchCorrectionFactorError", "Can''t process the file: unknown correction factor name ''{1}''", Level.SEVERE),
    CORRECTIONFACTOR_COLUMNS_LESS_THAN_2(9, "ColumnsNumberLessThan2", "Can''t process the file: minimal number of column is 2", Level.SEVERE),
    CORRECTIONFACTOR_TIMESTAMP_COLUMN_NOT_DEFINED(10, "TimestampColumnNotDefined", "Can''t process the file: ''Timestamp'' column is not specified in the file", Level.SEVERE),
    CORRECTIONFACTOR_DURATION_ATTRIBUTE_NOT_THE_SAME(11, "DurationAttributesNotTheSame", "Can''t process the file: the file must contain data for correction factors with the same value of ''Duration'' attribute", Level.SEVERE),
    CORRECTIONFACTOR_WRONG_TIMESTAMP(12, "WrongTimestamp", "Can''t process line {0}: wrong timestamp format", Level.SEVERE),
    CORRECTIONFACTOR_NOT_FOUND(13, "NoSuchCorrectionFactor", "Can''t process line {0}: unknown correction factor name ''{1}''", Level.SEVERE),
    CORRECTIONFACTOR_WRONG_VALUE(14, "WrongValueFormat", "Can''t process line {0}: wrong value format", Level.SEVERE),
    CORRECTIONFACTOR_TIMESTAMP_BEFORE_STARTTIME(15, "TimestampIsBeforeStarttime", "Can''t process line {0}: the timestamp must be greater or equal to {1}", Level.SEVERE),
    CORRECTIONFACTOR_WRONG_FIRST_TIMESTAMP(16, "WrongFirstTimestamp", "Can''t process line {0}: wrong first timestamp. First timestamp in the file should match to month, day and hour of ''Start time'' of all of the correction factors specified in the file.", Level.SEVERE),
    CORRECTIONFACTOR_WRONG_INTERVAL(17, "WrongInterval", "Can''t process line {0}: wrong timestamp. The interval between this and previous timestamp must be equal to ''Interval'' value of all of the correction factors specified in the file.", Level.SEVERE),
    CORRECTIONFACTOR_NOT_ENOUGH_DATA(18, "NotEnoughData", "Can''t process the file: the file must contain amount of data wich is multiple of ''Duration'' attribute value of all of the correction factors specified in the file", Level.SEVERE),
    CORRECTIONFACTOR_IMPORT_FAILED(19, "CorrectionFactorImportFailed", "Import failed", Level.SEVERE),

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
        return SyntheticLoadProfileFileImporterMessageHandler.COMPONENT_NAME;
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
        logger.log(getLevel(), formatMessage(thesaurus, args));
    }

    public void log(Logger logger, Thesaurus thesaurus, Throwable t, Object... args) {
        logger.log(getLevel(), formatMessage(thesaurus, args), t);
    }

    public String formatMessage(Thesaurus thesaurus, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        return format.format(args);
    }

    public enum Constants {
        ;

    }
}