/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed {
    DELETE_IMPORT_SCHEDULE_SQL_EXCEPTION(1, Keys.DELETE_IMPORT_SCHEDULE_SQL_EXCEPTION, "Import schedule {0} could not be removed. There was a problem accessing the database", Level.SEVERE),
    INVALIDCHARS(2, Keys.INVALIDCHARS_EXCEPTION, "This field contains invalid characters", Level.SEVERE),
    FIELD_IS_REQUIRED(3, Keys.FIELD_REQUIRED, "This field is required", Level.SEVERE);

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
        return FileImportApplication.COMPONENT_NAME;
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
        public static final String INVALIDCHARS_EXCEPTION = "InvalidCharsImporter";
        private static final String KEY_PREFIX = FileImportService.COMPONENT_NAME + '.';
        public static final String DELETE_IMPORT_SCHEDULE_SQL_EXCEPTION = "DeleteImportScheduleSqlException";
        public static final String FIELD_REQUIRED = "FieldRequired";
    }

}
