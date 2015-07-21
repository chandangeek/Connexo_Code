package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed, TranslationKey {

    DATE_FORMAT_IS_NOT_VALID(1, "DateFormatIsNotValid", "Invalid date format", Level.SEVERE),
    TIME_ZONE_IS_NOT_VALID(2, "TimeZoneIsNotValid", "Invalid time zone", Level.SEVERE),
    NUMBER_FORMAT_IS_INCOMPATIBLE_WITH_DELIMITER(3, "NumberFormatIncompatibleWithDelimiter", "Number format is incompatible with delimiter", Level.SEVERE),

    FILE_FORMAT_ERROR(4, "FileFormatError", "File format error: wrong number of columns in the line {0}. Importer service expects {1} but was {2}.", Level.SEVERE),
    LINE_MISSING_VALUE_ERROR(5, "LineMissingValueError", "Format error for line {0}: missing value for column ''{1}''.", Level.SEVERE),
    LINE_FORMAT_ERROR(6, "LineFormatError", "Format error for line {0}: wrong value format for column ''{1}'' (expected format = ''{2}'')", Level.SEVERE),
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
        return DeviceDataImporterMessageHandler.COMPONENT;
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

    public String getTranslated(Thesaurus thesaurus, Object... args){
        String translated = thesaurus.getString(this.getKey(), this.getDefaultFormat());
        return MessageFormat.format(translated, args);
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