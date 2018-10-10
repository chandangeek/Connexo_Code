/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;

import java.time.Clock;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TaskOccurrenceLauncher that queries for due tasks and creates a TaskOccurrence for each, then posts a message
 */
class DefaultTaskOccurrenceLauncher implements TaskOccurrenceLauncher {

    private static final Logger LOGGER = Logger.getLogger(DefaultTaskOccurrenceLauncher.class.getName());

    private final DueTaskFetcher dueTaskFetcher;
    private final TransactionService transactionService;
    private final ThreadPrincipalService threadPrincipalService;
    private final Clock clock;

    /**
     * @param threadPrincipalService
     * @param transactionService
     * @param dueTaskFetcher
     */
    public DefaultTaskOccurrenceLauncher(ThreadPrincipalService threadPrincipalService, TransactionService transactionService, DueTaskFetcher dueTaskFetcher, Clock clock) {
        this.threadPrincipalService = threadPrincipalService;
        this.transactionService = transactionService;
        this.dueTaskFetcher = dueTaskFetcher;
        this.clock = clock;
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
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        } finally {
            threadPrincipalService.clear();
        }
    }

    private void launchOccurrencesForDueTasks() {
        Instant now = clock.instant();
        getDueTasks(now).forEach(recurrentTask -> recurrentTask.launchOccurrence(now));
    }

    private TaskOccurrenceMessage asMessage(TaskOccurrence taskOccurrence) {
        return new TaskOccurrenceMessage(taskOccurrence);
    }

    private Iterable<RecurrentTaskImpl> getDueTasks(Instant at) {
        return dueTaskFetcher.dueTasks(at);
    }
}
