package com.elster.jupiter.tasks;

import com.elster.jupiter.util.logging.LogEntryFinder;

import java.time.Instant;
import java.util.List;
import java.util.logging.Level;

public interface TaskOccurrence {

    String getPayLoad();

    Instant getTriggerTime();

    RecurrentTask getRecurrentTask();

    void save();

    long getId();

    List<TaskLogEntry> getLogs();

    LogEntryFinder getLogsFinder();

    void log(Level level, Instant timestamp, String message);
}
