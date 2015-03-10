package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;

/**
 * TaskOccurrenceLauncher that queries for due tasks and creates a TaskOccurrence for each, then posts a message
 */
class DefaultTaskOccurrenceLauncher implements TaskOccurrenceLauncher {

    private final DueTaskFetcher dueTaskFetcher;
    private final TransactionService transactionService;
    private final ThreadPrincipalService threadPrincipalService;

    /**
     * @param threadPrincipalService
     * @param transactionService
     * @param dueTaskFetcher
     */
    public DefaultTaskOccurrenceLauncher(ThreadPrincipalService threadPrincipalService, TransactionService transactionService, DueTaskFetcher dueTaskFetcher) {
        this.threadPrincipalService = threadPrincipalService;
        this.transactionService = transactionService;
        this.dueTaskFetcher = dueTaskFetcher;
    }

    @Override
    public void run() {
        threadPrincipalService.set(() -> "TaskService");
        try {
            transactionService.execute(new VoidTransaction() {
                @Override
                protected void doPerform() {
                    launchOccurrencesForDueTasks();
                }
            });
        } finally {
            threadPrincipalService.clear();
        }
    }

    private void launchOccurrencesForDueTasks() {
        getDueTasks().forEach(RecurrentTaskImpl::launchOccurrence);
    }

    private TaskOccurrenceMessage asMessage(TaskOccurrence taskOccurrence) {
        return new TaskOccurrenceMessage(taskOccurrence);
    }

    private Iterable<RecurrentTaskImpl> getDueTasks() {
        return dueTaskFetcher.dueTasks();
    }
}
