package com.energyict.mdc.engine.impl.core;

import com.energyict.comserver.exceptions.CodingException;
import com.energyict.mdc.common.BusinessException;
import com.energyict.comserver.commands.DeviceCommandExecutionToken;
import com.energyict.comserver.commands.DeviceCommandExecutor;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.exceptions.PersistenceCodingException;
import com.energyict.mdc.protocol.api.exceptions.ConnectionSetupException;

import java.sql.SQLException;
import java.util.List;

/**
 * Executes {@link ScheduledJob}s in a transactional
 * context and correctly handles failures.
 * Upon failure, the actual task that failed will be rescheduled
 * and the {@link com.energyict.mdc.journal.ComSession} and/or
 * {@link com.energyict.mdc.journal.ComTaskExecutionSession}s
 * will be persisted in a separate transaction to make sure
 * that all information that describes the failure is available
 * for some process to diagnose later.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-10 (16:03)
 */
public abstract class ScheduledJobExecutor {

    // The minimum log level that needs to be set before the stacktrace of a error is logged to System.err
    private static final ComServer.LogLevel REQUIRED_DEBUG_LEVEL = ComServer.LogLevel.DEBUG;

    private ScheduledJobTransactionExecutor transactionExecutor;
    private ComServer.LogLevel logLevel;
    private DeviceCommandExecutor deviceCommandExecutor;

    public ScheduledJobExecutor(ScheduledJobTransactionExecutor transactionExecutor, ComServer.LogLevel logLevel, DeviceCommandExecutor deviceCommandExecutor) {
        this.transactionExecutor = transactionExecutor;
        this.logLevel = logLevel;
        this.deviceCommandExecutor = deviceCommandExecutor;
    }

    /**
     * Acquires a token and then executes the job.
     *
     * @param scheduledJob the job to execute
     */
    public void acquireTokenAndPerformSingleJob(ScheduledJob scheduledJob) {
        try {
            List<DeviceCommandExecutionToken> deviceCommandExecutionTokens = this.deviceCommandExecutor.acquireTokens(1);
            updateTokens(scheduledJob, deviceCommandExecutionTokens);
            this.execute(scheduledJob);
        }
        catch (InterruptedException e) {
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
         *    Slow connections such as mod-bus with lots of slave can take up to 20mins to complete. */
        ValidationTransaction validationTransaction = new ValidationTransaction(job);
        switch (this.transactionExecutor.execute(validationTransaction)) {
            case ATTEMPT_LOCK_SUCCESS: {
                try {
                    job.execute();
                    job.reschedule();
                } catch (ConnectionSetupException t) {
                    logIfDebuggingIsEnabled(t);
                    job.reschedule(t, RescheduleBehavior.RescheduleReason.CONNECTION_SETUP);
                } catch (Throwable t) {
                    logIfDebuggingIsEnabled(t);
                    job.reschedule(t, RescheduleBehavior.RescheduleReason.CONNECTION_BROKEN);
                } finally {
                    try {
                        Environment.DEFAULT.get().execute(new Transaction<Object>() {
                            @Override
                            public Object doExecute() throws BusinessException, SQLException {
                                job.unlock();
                                return null;
                            }
                        });
                    } catch (BusinessException e) {
                        throw CodingException.unexpectedBusinessException(e);
                    } catch (SQLException e) {
                        throw PersistenceCodingException.unexpectedSqlError(e);
                    }
                    Environment.DEFAULT.get().closeConnection();
                }
            }
            break;
            case JOB_OUTSIDE_COM_WINDOW: {
                job.rescheduleToNextComWindow();
            }
            break;
            case ATTEMPT_LOCK_FAILED:    // intentional fall through
            case NOT_PENDING_ANYMORE:   // intentional fall through
            default:
                job.releaseToken();
        }
    }

    private void logIfDebuggingIsEnabled(Throwable t) {
        if (REQUIRED_DEBUG_LEVEL.compareTo(logLevel) <= 0) {
            t.printStackTrace(System.err);
        }
    }

    private class ValidationTransaction implements Transaction<ValidationReturnStatus> {

        private ScheduledJob job;

        private ValidationTransaction(ScheduledJob job) {
            super();
            this.job = job;
        }

        @Override
        public synchronized ValidationReturnStatus doExecute() throws BusinessException, SQLException {
            if (!this.job.isStillPending()) {
                return ValidationReturnStatus.NOT_PENDING_ANYMORE;
            } else if (this.job.isWithinComWindow()) {
                return this.job.attemptLock() ? ValidationReturnStatus.ATTEMPT_LOCK_SUCCESS : ValidationReturnStatus.ATTEMPT_LOCK_FAILED;
            } else {
                return ValidationReturnStatus.JOB_OUTSIDE_COM_WINDOW;
            }
        }
    }

    public enum ValidationReturnStatus {
        ATTEMPT_LOCK_SUCCESS,
        ATTEMPT_LOCK_FAILED,
        JOB_OUTSIDE_COM_WINDOW,
        NOT_PENDING_ANYMORE;
    }

}