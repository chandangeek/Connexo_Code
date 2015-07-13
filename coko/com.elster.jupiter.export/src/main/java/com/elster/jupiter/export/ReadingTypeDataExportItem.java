package com.elster.jupiter.export;

import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Optional;

public interface ReadingTypeDataExportItem {
    ReadingTypeDataSelector getSelector();

    ReadingContainer getReadingContainer();

    ReadingType getReadingType();

    Optional<Instant> getLastRun();

    Optional<Range<Instant>> getLastExportPeriod();

    Optional<Instant> getLastExportedDate();

    boolean isActive();

    Optional<? extends DataExportOccurrence> getLastOccurrence();
}
