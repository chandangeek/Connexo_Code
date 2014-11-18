package com.elster.jupiter.tasks;

import java.time.Instant;
import java.util.List;

public interface TaskOccurrence {

    String getPayLoad();

    Instant getTriggerTime();

    RecurrentTask getRecurrentTask();

    void save();

    long getId();

    List<TaskLogEntry> getLogs();

    TaskLogHandler createTaskLogHandler();
}
