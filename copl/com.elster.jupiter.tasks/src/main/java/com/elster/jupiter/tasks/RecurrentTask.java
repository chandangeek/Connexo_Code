package com.elster.jupiter.tasks;

import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.time.Clock;

import java.util.Date;

/**
 * Models a task that should put a message on the task queue recurrently.
 */
public interface RecurrentTask extends HasName {

    long getId();

    void updateNextExecution(Clock clock);

    String getDestination();

    String getPayLoad();

    Date getNextExecution();

    TaskOccurrence createTaskOccurrence(Clock clock);

    void save();

    void delete();
}
