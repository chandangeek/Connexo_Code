package com.elster.jupiter.tasks.rest.impl;

import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;

import java.util.Optional;

/**
 * Created by igh on 27/10/2015.
 */
public class TaskInfo {

    public String name;
    public String application;
    public String queue;
    public String queueStatus = "";

    public TaskInfo(RecurrentTask recurrentTask) {
        name = recurrentTask.getName();
        application = "Multisense";
        queue = recurrentTask.getDestination().getQueueTableSpec().getName();
        Optional<TaskOccurrence> lastOccurrence = recurrentTask.getLastOccurrence();
        if (lastOccurrence.isPresent()) {
            queueStatus = lastOccurrence.get().getStatus().getDefaultFormat();
        }

    }
}
