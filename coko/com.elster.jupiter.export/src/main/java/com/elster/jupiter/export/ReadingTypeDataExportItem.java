package com.elster.jupiter.export;

import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingType;

import java.time.Instant;
import java.util.Optional;

public interface ReadingTypeDataExportItem {
    ReadingTypeDataSelector getSelector();

    ReadingContainer getReadingContainer();

    ReadingType getReadingType();

    Optional<Instant> getLastRun();

    Optional<Instant> getLastExportedDate();

    boolean isActive();

    Optional<? extends DataExportOccurrence> getLastOccurrence();
}
