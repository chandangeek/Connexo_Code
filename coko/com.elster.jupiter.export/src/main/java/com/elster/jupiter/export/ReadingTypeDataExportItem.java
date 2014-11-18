package com.elster.jupiter.export;

import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingType;

import java.time.Instant;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 5/11/2014
 * Time: 13:49
 */
public interface ReadingTypeDataExportItem {
    ReadingTypeDataExportTask getTask();

    ReadingContainer getReadingContainer();

    ReadingType getReadingType();

    Optional<Instant> getLastRun();

    Optional<Instant> getLastExportedDate();

    boolean isActive();
}
