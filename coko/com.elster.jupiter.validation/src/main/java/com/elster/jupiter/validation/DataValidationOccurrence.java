/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation;

import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.logging.LogEntry;
import com.elster.jupiter.util.logging.LogEntryFinder;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


@ProviderType
public interface DataValidationOccurrence {

    DataValidationTask getTask();

    Optional<Instant> getStartDate();

    Optional<Instant> getEndDate();

    String getFailureReason();

    DataValidationTaskStatus getStatus();

    /**
     * Returns the name of the status of this DataValidationOccurrence
     * in the user's preferred language.
     *
     * @return The name of the status of this DataValidationOccurrence
     */
    String getStatusName();

    Instant getTriggerTime();

    boolean wasScheduled();

    Long getId();

    List<? extends LogEntry> getLogs();

    LogEntryFinder getLogsFinder();

    TaskOccurrence getTaskOccurrence();

    void end(DataValidationTaskStatus status, String message);

    void end(DataValidationTaskStatus status);

}