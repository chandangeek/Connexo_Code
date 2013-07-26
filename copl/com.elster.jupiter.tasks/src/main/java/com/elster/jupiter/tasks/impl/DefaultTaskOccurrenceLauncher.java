package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.transaction.VoidTransaction;

public class DefaultTaskOccurrenceLauncher implements TaskOccurrenceLauncher {

    private final DueTaskFetcher dueTaskFetcher;

    public DefaultTaskOccurrenceLauncher(DueTaskFetcher dueTaskFetcher) {
        this.dueTaskFetcher = dueTaskFetcher;
    }

    @Override
    public void run() {
        Bus.getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                launchOccurrencesForDueTasks();
            }
        });
    }

    private void launchOccurrencesForDueTasks() {
        for (RecurrentTask recurrentTask : getDueTasks()) {
            TaskOccurrence taskOccurrence = recurrentTask.createTaskOccurrence(Bus.getClock());
            String json = toJson(taskOccurrence);
            recurrentTask.getDestination().message(json).send();
            recurrentTask.updateNextExecution(Bus.getClock());
            recurrentTask.save();
        }
    }

    private String toJson(TaskOccurrence taskOccurrence) {
        return Bus.getJsonService().serialize(asMessage(taskOccurrence));
    }

    private TaskOccurrenceMessage asMessage(TaskOccurrence taskOccurrence) {
        return new TaskOccurrenceMessage(taskOccurrence);
    }

    private Iterable<RecurrentTask> getDueTasks() {
        return dueTaskFetcher.dueTasks();
    }
}
