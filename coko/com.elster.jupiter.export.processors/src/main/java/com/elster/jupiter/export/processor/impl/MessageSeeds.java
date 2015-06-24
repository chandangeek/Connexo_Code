package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed, TranslationKey {
    INVALIDCHARS_EXCEPTION(1001, Keys.INVALIDCHARS_EXCEPTION, "Characters {0} are not allowed.", Level.SEVERE),
    INVALID_READING_CONTAINER(1002, Keys.INVALID_READING_CONTAINER, "Reading container is not a Meter", Level.WARNING),
    FILE_IO(1003, Keys.FILE_IO, "Failure while doing IO on file {0}", Level.SEVERE),
    ABSOLUTE_PATH_NOT_ALLOWED(1004, Keys.ABSOLUTE_PATH, "Absolute path is not allowed here", Level.SEVERE);

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
        public static final String INVALIDCHARS_EXCEPTION = "InvalidChars";
        public static final String INVALID_READING_CONTAINER = "InvalidReadingContainer";
        public static final String FILE_IO = "file.io.failure";
        public static final String ABSOLUTE_PATH = "path.absolute.disallowed";
    }

}

