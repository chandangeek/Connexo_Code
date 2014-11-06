package com.elster.jupiter.export;

import com.elster.jupiter.metering.ReadingContainer;

import java.time.Instant;

/**
 * Copyrights EnergyICT
 * Date: 5/11/2014
 * Time: 13:49
 */
public interface ReadingTypeDataExportItem {
    Instant getLastRun();

    ReadingContainer getReadingContainer();

    String getReadingTypeMRId();

    Instant getLastExportedDate();

    ReadingTypeDataExportTask getTask();
}
