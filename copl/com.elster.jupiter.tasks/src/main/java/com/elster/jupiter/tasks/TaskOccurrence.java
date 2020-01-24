/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks;

import com.elster.jupiter.util.logging.LogEntryFinder;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface TaskOccurrence {

    String getPayLoad();

    Instant getTriggerTime();

    Optional<Instant> getRetryTime();

    Optional<Instant> getAdhocTime();

    RecurrentTask getRecurrentTask();

    Optional<Instant> getStartDate();

    Optional<Instant> getEndDate();

    TaskStatus getStatus();

    /**
     * Returns the name of the status of this TaskOccurrence
     * in the user's preferred language.
     *
     * @return The name of the status of this TaskOccurrence
     */
    String getStatusName();

    void save();

    long getId();

    List<TaskLogEntry> getLogs();

    LogEntryFinder getLogsFinder();

    TaskLogHandler createTaskLogHandler();

    TaskLogHandler createTaskLogHandler(RecurrentTask recurrentTask);

    boolean wasScheduled();

    void start();
    
    void setToFailed();
}
