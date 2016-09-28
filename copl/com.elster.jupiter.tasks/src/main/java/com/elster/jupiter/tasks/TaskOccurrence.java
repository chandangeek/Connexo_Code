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

    RecurrentTask getRecurrentTask();

    Optional<Instant> getStartDate();

    Optional<Instant> getEndDate();

    TaskStatus getStatus();

    void save();

    long getId();

    List<TaskLogEntry> getLogs();

    LogEntryFinder getLogsFinder();

    TaskLogHandler createTaskLogHandler();

    boolean wasScheduled();

    void start();
}
