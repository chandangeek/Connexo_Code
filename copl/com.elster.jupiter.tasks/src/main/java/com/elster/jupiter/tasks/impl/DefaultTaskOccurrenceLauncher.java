package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.json.JsonService;

/**
 * TaskOccurrenceLauncher that queries for due tasks and creates a TaskOccurrence for each, then posts a message
 */
class DefaultTaskOccurrenceLauncher implements TaskOccurrenceLauncher {

    private final DueTaskFetcher dueTaskFetcher;
    private final TransactionService transactionService;
    private final JsonService jsonService;

    /**
     * @param transactionService
     * @param jsonService
     * @param dueTaskFetcher
     */
    public DefaultTaskOccurrenceLauncher(TransactionService transactionService, JsonService jsonService, DueTaskFetcher dueTaskFetcher) {
        this.transactionService = transactionService;
        this.jsonService = jsonService;
        this.dueTaskFetcher = dueTaskFetcher;
    }

    @Override
    public void run() {
        transactionService.execute(new VoidTransaction() {
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
        return jsonService.serialize(asMessage(taskOccurrence));
    }

    private TaskOccurrenceMessage asMessage(TaskOccurrence taskOccurrence) {
        return new TaskOccurrenceMessage(taskOccurrence);
    }

    private Iterable<RecurrentTask> getDueTasks() {
        return dueTaskFetcher.dueTasks();
    }
}
