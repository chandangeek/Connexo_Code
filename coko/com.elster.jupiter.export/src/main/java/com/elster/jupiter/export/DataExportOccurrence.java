package com.elster.jupiter.export;

import com.elster.jupiter.util.logging.LogEntry;
import com.elster.jupiter.util.logging.LogEntryFinder;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DataExportOccurrence {

    Instant getStartDate();

    Optional<Instant> getEndDate();

    DataExportStatus getStatus();

    String getFailureReason();

    Range<Instant> getExportedDataInterval();

    Instant getTriggerTime();

    Long getId();

    List<? extends LogEntry> getLogs();

    LogEntryFinder getLogsFinder();
}
