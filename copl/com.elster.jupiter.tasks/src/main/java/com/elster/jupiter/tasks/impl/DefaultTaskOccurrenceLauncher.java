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
                try {
                    launchOccurrencesForDueTasks();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void launchOccurrencesForDueTasks() {
        for (RecurrentTask recurrentTask : getDueTasks()) {
            TaskOccurrence taskOccurrence = recurrentTask.createTaskOccurrence(Bus.getClock());
            recurrentTask.getDestination().send(taskOccurrence.getPayLoad());
            taskOccurrence.save();
            recurrentTask.updateNextExecution(Bus.getClock());
            recurrentTask.save();
        }
    }

    private Iterable<RecurrentTask> getDueTasks() {
        return dueTaskFetcher.dueTasks();
    }
}
