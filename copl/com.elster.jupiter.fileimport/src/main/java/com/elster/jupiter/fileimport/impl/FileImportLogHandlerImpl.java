package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportLogHandler;

import java.time.Instant;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


public class FileImportLogHandlerImpl extends Handler implements FileImportLogHandler {

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
        fileImport.log(record.getLevel(), Instant.ofEpochMilli(record.getMillis()), record.getMessage());
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }
}
