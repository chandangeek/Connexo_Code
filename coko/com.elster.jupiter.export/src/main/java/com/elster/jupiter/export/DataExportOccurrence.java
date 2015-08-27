package com.elster.jupiter.export;

import com.elster.jupiter.util.logging.LogEntry;
import com.elster.jupiter.util.logging.LogEntryFinder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DataExportOccurrence {

    ExportTask getTask();

    Optional<Instant> getStartDate();

    Optional<Instant> getEndDate();

    DataExportStatus getStatus();

    String getFailureReason();

    Optional<DefaultSelectorOccurrence> getDefaultSelectorOccurrence();

    Instant getTriggerTime();

    boolean wasScheduled();

    Long getId();

    List<? extends LogEntry> getLogs();

    LogEntryFinder getLogsFinder();

    int nthSince(Instant since);
}
