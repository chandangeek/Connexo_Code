/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.util.logging.LogEntry;
import com.elster.jupiter.util.logging.LogEntryFinder;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface DataExportOccurrence {

    ExportTask getTask();

    Optional<Instant> getStartDate();

    Optional<Instant> getEndDate();

    DataExportStatus getStatus();

    /**
     * Returns the name of the status of this DataExportOccurrence
     * in the user's preferred language.
     *
     * @return The name of the status of this DataExportOccurrence
     */
    String getStatusName();

    String getFailureReason();

    String getSummary();

    Optional<DefaultSelectorOccurrence> getDefaultSelectorOccurrence();

    Instant getTriggerTime();

    boolean wasScheduled();

    Long getId();

    List<? extends LogEntry> getLogs();

    LogEntryFinder getLogsFinder();

    int nthSince(Instant since);
}
