/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class FileImportLogHandlerImpl extends Handler implements FileImportLogHandler {
    private List<LogRecord> logRecords = new ArrayList<>();

    private final FileImportOccurrenceImpl fileImport;

    private final int level;

    public FileImportLogHandlerImpl(FileImportOccurrenceImpl fileImport) {
        this.fileImport = fileImport;
        this.level = fileImport.getImportSchedule().getLogLevel();
    }

    @Override
    public Handler asHandler() {
        return this;
    }

    @Override
    public void publish(LogRecord record) {
        if (record.getLevel().intValue() >= level ) {
            logRecords.add(record);
        }
    }

    @Override
    public void flush() {

    }

    @Override
    public void saveLogEntries() {
        for (LogRecord record : logRecords) {
            Throwable thrown = record.getThrown();
            if (thrown == null) {
                fileImport.log(record.getLevel(), Instant.ofEpochMilli(record.getMillis()), record.getMessage());
            } else {
                fileImport.log(Instant.ofEpochMilli(record.getMillis()), record.getMessage(), thrown);
            }
        }
        logRecords = new ArrayList<>();
    }

    @Override
    public void close() throws SecurityException {

    }
}
