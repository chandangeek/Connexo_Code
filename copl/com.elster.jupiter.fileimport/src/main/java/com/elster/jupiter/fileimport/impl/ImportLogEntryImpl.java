package com.elster.jupiter.fileimport.impl;


import com.elster.jupiter.fileimport.ImportLogEntry;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.time.Instant;
import java.util.logging.Level;

public class ImportLogEntryImpl implements ImportLogEntry {

    private Reference<FileImportOccurrence> fileImportOccurrenceReference = ValueReference.absent();
    private int position;
    private Instant timeStamp;
    private String message;
    private String stackTrace;
    private int level;

    @Inject
    ImportLogEntryImpl() {
    }

    ImportLogEntryImpl init(FileImportOccurrence occurrence, Instant timeStamp, Level level, String message) {
        this.fileImportOccurrenceReference.set(occurrence);
        this.timeStamp = timeStamp;
        this.level = level.intValue();
        this.message = message.trim().substring(0, Math.min(message.trim().length(), Table.DESCRIPTION_LENGTH));
        return this;
    }

    @Override
    public FileImportOccurrence getFileImportOccurrence() {
        return fileImportOccurrenceReference.get();
    }

    @Override
    public Instant getTimestamp() {
        return timeStamp;
    }

    @Override
    public Level getLogLevel() {
        return Level.parse(Integer.toString(level));
    }

    @Override
    public String getMessage() {
        return message;
    }
}
