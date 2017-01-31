/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;

import java.sql.Connection;
import java.sql.SQLException;
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

    Connection getCurrentConnection() throws SQLException;
}
