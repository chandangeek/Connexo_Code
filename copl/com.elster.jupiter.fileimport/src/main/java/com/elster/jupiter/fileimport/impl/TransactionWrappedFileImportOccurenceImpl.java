/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.ImportLogEntry;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.fileimport.Status;
import com.elster.jupiter.transaction.TransactionService;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Created by bbl on 2/12/2015.
 */
class TransactionWrappedFileImportOccurenceImpl implements FileImportOccurrence {

    private final ServerFileImportOccurrence fileImportOccurrence;
    private final TransactionService transactionService;
    private final BufferedLogHandler logHandler;

    TransactionWrappedFileImportOccurenceImpl(TransactionService transactionService, ServerFileImportOccurrence fileImportOccurrence) {
        this.transactionService = transactionService;
        this.fileImportOccurrence = fileImportOccurrence;
        this.logHandler = new BufferedLogHandler();
    }

    @Override
    public InputStream getContents() {
        return fileImportOccurrence.getContents();
    }

    @Override
    public String getFileName() {
        return fileImportOccurrence.getFileName();
    }

    @Override
    public Status getStatus() {
        return fileImportOccurrence.getStatus();
    }

    @Override
    public String getStatusName() {
        return fileImportOccurrence.getStatusName();
    }

    @Override
    public void markSuccess(String message) throws IllegalStateException {
        logHandler.flushRecordsTransactionally();
        transactionService.run(() -> fileImportOccurrence.markSuccess(message));
    }

    @Override
    public void markSuccessWithFailures(String message) throws IllegalStateException {
        logHandler.flushRecordsTransactionally();
        transactionService.run(() -> fileImportOccurrence.markSuccessWithFailures(message));
    }

    @Override
    public void markFailure(String message) {
        logHandler.flushRecordsTransactionally();
        transactionService.run(() -> fileImportOccurrence.markFailure(message));
    }

    @Override
    public long getId() {
        return fileImportOccurrence.getId();
    }

    @Override
    public ImportSchedule getImportSchedule() {
        return fileImportOccurrence.getImportSchedule();
    }

    @Override
    public Optional<Instant> getStartDate() {
        return fileImportOccurrence.getStartDate();
    }

    @Override
    public Optional<Instant> getEndDate() {
        return fileImportOccurrence.getEndDate();
    }

    @Override
    public Instant getTriggerDate() {
        return fileImportOccurrence.getTriggerDate();
    }

    @Override
    public List<ImportLogEntry> getLogs() {
        return fileImportOccurrence.getLogs();
    }

    @Override
    public Logger getLogger() {
        Logger anonymousLogger = fileImportOccurrence.getLogger();
        Optional<Handler> toRemove = Arrays.stream(anonymousLogger.getHandlers()).filter(FileImportLogHandler.class::isInstance).findFirst();
        if (toRemove.isPresent()) {
            anonymousLogger.removeHandler(toRemove.get());
            anonymousLogger.addHandler(logHandler);
        }
        return anonymousLogger;
    }

    @Override
    public Finder<ImportLogEntry> getLogsFinder() {
        return fileImportOccurrence.getLogsFinder();
    }

    @Override
    public String getMessage() {
        return fileImportOccurrence.getMessage();
    }

    private class BufferedLogHandler extends Handler {

        private static final int BUFFER_SIZE = 100;
        private List<LogRecord> logRecords;

        BufferedLogHandler() {
            initLogRecords();
        }

        private void initLogRecords() {
            logRecords = new ArrayList<>(BUFFER_SIZE);
        }

        @Override
        public void publish(LogRecord record) {
            synchronized (this) {
                if (logRecords.size() == BUFFER_SIZE) {
                    flushRecordsTransactionally();
                }
                logRecords.add(record);
            }
        }

        private void flushRecordsTransactionally() {
            transactionService.run(this::flushRecords);

        }

        private void flushRecords() {
            synchronized (this) {
                logRecords.forEach(recordToLog -> fileImportOccurrence.log(recordToLog.getLevel(), Instant.ofEpochMilli(recordToLog.getMillis()), recordToLog.getMessage()));
                initLogRecords();
            }
        }

        @Override
        public void flush() {
            flushRecords();
        }

        @Override
        public void close() throws SecurityException {
            flushRecords();
        }
    }
}
