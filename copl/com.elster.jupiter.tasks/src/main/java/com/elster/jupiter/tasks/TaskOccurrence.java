package com.elster.jupiter.tasks;

import com.elster.jupiter.util.logging.LogEntryFinder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TaskOccurrence {

    String getPayLoad();

    Instant getTriggerTime();

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

    boolean wasScheduled();
}
