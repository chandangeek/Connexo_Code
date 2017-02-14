/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed {
    DELETE_TASK_STATUS_BUSY(1, Keys.DELETE_TASK_STATUS_BUSY, "The data export task cannot be removed because the task is running at this moment.", Level.SEVERE),
    DELETE_TASK_SQL_EXCEPTION(2, Keys.DELETE_TASK_SQL_EXCEPTION, "Data export task {0} could not be removed. There was a problem accessing the database", Level.SEVERE),
    FIELD_IS_REQUIRED(3, Keys.REQUIRED_FIELD, "This field is required", Level.SEVERE),
    RUN_TASK_CONCURRENT_TITLE(4, "RunTaskConcurrentTitle", "Failed to run ''{0}''", Level.SEVERE),
    RUN_TASK_CONCURRENT_BODY(5, "RunTaskConcurrentMessage", "{0} has changed since the page was last updated.", Level.SEVERE),
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
        private static final String KEY_PREFIX = DataExportService.COMPONENTNAME + '.';
        public static final String DELETE_TASK_STATUS_BUSY = "DeleteTaskStatusBusy";
        public static final String DELETE_TASK_SQL_EXCEPTION = "DeleteTaskSqlException";
        public static final String REQUIRED_FIELD = "RequiredField";
    }

}