package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.transaction.VoidTransaction;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

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
            recurrentTask.getDestination().send(json);
            recurrentTask.updateNextExecution(Bus.getClock());
            recurrentTask.save();
        }
    }

    private String toJson(TaskOccurrence taskOccurrence) {
        TaskOccurrenceMessage taskOccurrenceMessage = new TaskOccurrenceMessage(taskOccurrence);
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(taskOccurrenceMessage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Iterable<RecurrentTask> getDueTasks() {
        return dueTaskFetcher.dueTasks();
    }
}
