package com.elster.jupiter.export;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Optional;

public interface DataExportOccurrence {

    Instant getStartDate();

    Optional<Instant> getEndDate();

    DataExportStatus getStatus();

    Range<Instant> getExportedDataInterval();

    Instant getTriggerTime();
}
