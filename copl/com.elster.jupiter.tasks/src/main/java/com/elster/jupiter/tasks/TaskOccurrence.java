package com.elster.jupiter.tasks;

import com.elster.jupiter.util.logging.LogEntryFinder;

import java.time.Instant;
import java.util.List;

public interface TaskOccurrence {

    String getPayLoad();

    Instant getTriggerTime();

    RecurrentTask getRecurrentTask();

    void save();

    long getId();

    List<TaskLogEntry> getLogs();

    LogEntryFinder getLogsFinder();

    TaskLogHandler createTaskLogHandler();
}
