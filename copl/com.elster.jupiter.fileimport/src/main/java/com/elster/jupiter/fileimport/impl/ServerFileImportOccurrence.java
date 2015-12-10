package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;

import java.time.Instant;
import java.util.logging.Level;

/**
 * Created by bbl on 7/12/2015.
 */
public interface ServerFileImportOccurrence extends FileImportOccurrence {
    void prepareProcessing();

    void setStartDate(Instant instant);

    void save();

    void log(Level level, Instant instant, String message);
}
