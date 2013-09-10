package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.transaction.VoidTransaction;

/**
 * TaskOccurrenceLauncher that queries for due tasks and creates a TaskOccurrence for each, then posts a message
 */
class DefaultTaskOccurrenceLauncher implements TaskOccurrenceLauncher {

    private final DueTaskFetcher dueTaskFetcher;

    /**
     * @param dueTaskFetcher
     */
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
            TaskOccurrence taskOccurrence = recurrentTask.createTaskOccurrence();
            String json = toJson(taskOccurrence);
            recurrentTask.getDestination().message(json).send();
            recurrentTask.updateNextExecution();
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
