package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;

public class TaskOccurrenceLauncher implements Runnable {

    private final DueTaskFetcher dueTaskFetcher;

    public TaskOccurrenceLauncher(DueTaskFetcher dueTaskFetcher) {
        this.dueTaskFetcher = dueTaskFetcher;
    }

    @Override
    public void run() {
        launchOccurrencesForDueTasks();
    }

    private void launchOccurrencesForDueTasks() {
        for (RecurrentTask recurrentTask : getDueTasks()) {
            TaskOccurrence taskOccurrence = recurrentTask.createTaskOccurrence(Bus.getClock());
            DestinationSpec destinationSpec = getDestinationSpec(recurrentTask);
            destinationSpec.send(taskOccurrence.getPayLoad());
            taskOccurrence.save();
        }
    }

    private DestinationSpec getDestinationSpec(RecurrentTask recurrentTask) {
        return Bus.getMessageService().getDestinationSpec(recurrentTask.getDestination());
    }

    private Iterable<RecurrentTask> getDueTasks() {
        return dueTaskFetcher.dueTasks();
    }
}
