package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed {
    IMPORT_USAGEPOINT_SUCCEEDED(1010, Constants.IMPORT_SUCCEEDED, " ''{0}'' usage points successfully imported without any errors", Level.INFO),
    IMPORT_USAGEPOINT_SUCCEEDED_WITH_FAILURES(1011, Constants.IMPORT_SUCCEEDED_WITH_FAILURES, " ''{0}'' usage points successfully imported, ''{1}'' usage points failed", Level.WARNING),
    IMPORT_USAGEPOINT_EXCEPTION(1012, Constants.IMPORT_USAGEPOINT_EXCEPTION, " Import failed. Please check file content format", Level.SEVERE),
    IMPORT_USAGEPOINT_INVALIDDATA(1013, Constants.IMPORT_USAGEPOINT_INVALID_DATA, " Invalid data in line ''{0}''", Level.WARNING),
    IMPORT_USAGEPOINT_SERVICEKIND_INVALID(1014, Constants.IMPORT_USAGEPOINT_INVALID_SERVICEKIND, " Invalid service kind in line ''{0}''", Level.WARNING),
    IMPORT_USAGEPOINT_SERVICELOCATION_INVALID(1015, Constants.IMPORT_USAGEPOINT_INVALID_SERVICELOCATION, " Invalid service location in line ''{0}''. Attribute skipped.", Level.WARNING),
    IMPORT_USAGEPOINT_MRID_INVALID(1016, Constants.IMPORT_USAGEPOINT_INVALID_MRID, " Invalid mrid in line ''{0}''", Level.WARNING),
    IMPORT_USAGEPOINT_PARSER_INVALID(1017, Constants.IMPORT_PARSER_INVALID, " Parser not found for ''{0}''", Level.WARNING);

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
        public static final String IMPORT_SUCCEEDED = "up.import.succeeded";
        public static final String IMPORT_SUCCEEDED_WITH_FAILURES = "up.import.succeeded.with.failures";
        public static final String IMPORT_USAGEPOINT_EXCEPTION = "up.import.exception";
        public static final String IMPORT_USAGEPOINT_INVALID_MRID = "up.invalid.mrid";
        public static final String IMPORT_USAGEPOINT_INVALID_DATA = "up.invalid.date";
        public static final String IMPORT_USAGEPOINT_INVALID_SERVICEKIND = "up.invalid.service.kind";
        public static final String IMPORT_USAGEPOINT_INVALID_SERVICELOCATION = "up.invalid.service.location";
        public static final String IMPORT_PARSER_INVALID = "up.invalid.parser";
    }
}