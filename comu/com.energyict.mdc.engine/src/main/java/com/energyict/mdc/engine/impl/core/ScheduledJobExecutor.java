/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.io.ConnectionCommunicationException;
import com.energyict.mdc.protocol.api.exceptions.ConnectionSetupException;

import java.util.List;

/**
 * Executes {@link ScheduledJob}s in a transactional
 * context and correctly handles failures.
 * Upon failure, the actual task that failed will be rescheduled
 * and the ComSession and/or ComTaskExecutionSessions
 * will be persisted in a separate transaction to make sure
 * that all information that describes the failure is available
 * for some process to diagnose later.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-10 (16:03)
 */
abstract class ScheduledJobExecutor {

    // The minimum log level that needs to be set before the stacktrace of a error is logged to System.err
    private static final ComServer.LogLevel REQUIRED_DEBUG_LEVEL = ComServer.LogLevel.DEBUG;

    private final TransactionService transactionService;
    private final ComServer.LogLevel logLevel;
    private final DeviceCommandExecutor deviceCommandExecutor;

    ScheduledJobExecutor(TransactionService transactionService, ComServer.LogLevel logLevel, DeviceCommandExecutor deviceCommandExecutor) {
        this.transactionService = transactionService;
        this.logLevel = logLevel;
        this.deviceCommandExecutor = deviceCommandExecutor;
    }

    /**
     * Acquires a token and then executes the job.
     *
     * @param scheduledJob the job to execute
     */
    void acquireTokenAndPerformSingleJob(ScheduledJob scheduledJob) {
        try {
            List<DeviceCommandExecutionToken> deviceCommandExecutionTokens = this.deviceCommandExecutor.acquireTokens(1);
            updateTokens(scheduledJob, deviceCommandExecutionTokens);
            this.execute(scheduledJob);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void updateTokens(ScheduledJob scheduledJob, List<DeviceCommandExecutionToken> deviceCommandExecutionTokens) {
        for (int i = 0; i < deviceCommandExecutionTokens.size(); i++) {
            DeviceCommandExecutionToken token = deviceCommandExecutionTokens.get(i);
            if (i == 0) {
                scheduledJob.setToken(token);
            } else {
                // just to be safe, free the tokens if you have any left ...
                this.deviceCommandExecutor.free(token);
            }
        }
    }

    public void execute(final ScheduledJob job) {
        /* Essential to design:
         * First run validation transaction that attempts to lock.
         *    If not already locked then the lock should be committed as soon as possible
         *    The database lock is released and replaced by a business lock, i.e.
         *    a database attribute is set such that the task is no longer picked
         *    up by the task query.
         * Once lock succeeds, we can execute the actual job.
         *    The execution of the job however should NOT be done in a big transaction
         *    context because as soon as an update is done on an object
         *    (e.g. the lastCommunicationStart is set on the ConnectionTask
         *    the database will lock that object for the duration of the transaction.
         *    External application that want to update the object will block
         *    until the lock is released, i.e. until the job's execution completed.
         *    Slow connections such as mod-bus with lots of slaves can take up to 20mins to complete. */
        ValidationTransaction validationTransaction = new ValidationTransaction(job);
        ValidationReturnStatus validationReturnStatus;
        try {
            validationReturnStatus = this.transactionService.execute(validationTransaction);
        } catch (UnderlyingSQLFailedException e) {
            job.releaseToken();
            return;
        }
        switch (validationReturnStatus) {
            case ATTEMPT_LOCK_SUCCESS: {
                try {
                    job.execute();
                } catch (Throwable t) {
                    logIfDebuggingIsEnabled(t);
                } finally {
                    job.reschedule();
                }
            }
            break;
            case JOB_OUTSIDE_COM_WINDOW: {
                job.rescheduleToNextComWindow();
            }
            break;
            case ATTEMPT_LOCK_FAILED:   // intentional fall through
            case NOT_PENDING_ANYMORE:   // intentional fall through
            default: {
                job.releaseToken();
            }
        }
    }

    private void logIfDebuggingIsEnabled(Throwable t) {
        if (REQUIRED_DEBUG_LEVEL.compareTo(logLevel) <= 0) {
            if (!(t instanceof ConnectionSetupException || t instanceof ConnectionCommunicationException)) {
                t.printStackTrace(System.err);
            }
        }
    }

    public enum ValidationReturnStatus {
        ATTEMPT_LOCK_SUCCESS,
        ATTEMPT_LOCK_FAILED,
        JOB_OUTSIDE_COM_WINDOW,
        NOT_PENDING_ANYMORE
    }

    private class ValidationTransaction implements Transaction<ValidationReturnStatus> {

        private ScheduledJob job;

        private ValidationTransaction(ScheduledJob job) {
            super();
            this.job = job;
        }

        @Override
        public synchronized ValidationReturnStatus perform() {
            if (!this.job.isStillPending()) {
                return ValidationReturnStatus.NOT_PENDING_ANYMORE;
            } else {
                boolean attemptLock = this.job.attemptLock();
                if (attemptLock) {
                    if (this.job.isWithinComWindow()) {
                        return ValidationReturnStatus.ATTEMPT_LOCK_SUCCESS;
                    } else {
                        return ValidationReturnStatus.JOB_OUTSIDE_COM_WINDOW;
                    }
                } else {
                    return ValidationReturnStatus.ATTEMPT_LOCK_FAILED;
                }
            }
        }
    }

}