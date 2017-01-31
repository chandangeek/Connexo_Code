/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.ImportSchedule;

import java.nio.file.Path;
import java.time.Clock;

/**
 * Created by bbl on 7/12/2015.
 */
public interface ServerImportSchedule extends ImportSchedule {
    ServerFileImportOccurrence createFileImportOccurrence(Path file, Clock clock);
}
