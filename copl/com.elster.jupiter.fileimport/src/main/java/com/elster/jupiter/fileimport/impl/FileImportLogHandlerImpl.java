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

    private List<LogRecord> logRecords = new ArrayList<LogRecord>();

    private FileImportOccurrenceImpl fileImport;

    public FileImportLogHandlerImpl(FileImportOccurrenceImpl fileImport){
        this.fileImport = fileImport;
    }

    @Override
    public Handler asHandler() {
        return this;
    }

    @Override
    public void publish(LogRecord record) {
        logRecords.add(record);
        //fileImport.log(record.getLevel(), Instant.ofEpochMilli(record.getMillis()), record.getMessage());
    }

    @Override
    public void flush() {

    }

    @Override
    public void saveLogEntries() {
        for (LogRecord record : logRecords) {
            fileImport.log(record.getLevel(), Instant.ofEpochMilli(record.getMillis()), record.getMessage());
        }
        logRecords = new ArrayList<>();
    }

    @Override
    public void close() throws SecurityException {

    }
}
