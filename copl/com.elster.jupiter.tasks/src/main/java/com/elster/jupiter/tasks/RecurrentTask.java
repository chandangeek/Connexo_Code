package com.elster.jupiter.tasks;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.util.HasName;

import java.util.Date;

/**
 * Models a task that should put a message on the task queue recurrently.
 */
public interface RecurrentTask extends HasName {

    long getId();

    void updateNextExecution();

    DestinationSpec getDestination();

    String getPayLoad();

    Date getNextExecution();

    TaskOccurrence createTaskOccurrence();

    void save();

    void delete();

    void suspend();

    void resume();
}
