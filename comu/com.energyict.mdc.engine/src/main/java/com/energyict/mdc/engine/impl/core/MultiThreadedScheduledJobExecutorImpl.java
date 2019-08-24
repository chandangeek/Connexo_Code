/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

class MultiThreadedScheduledJobExecutorImpl extends ScheduledJobExecutor implements MultiThreadedScheduledJobExecutor {

    private static final Logger LOGGER = Logger.getLogger(MultiThreadedScheduledJobExecutorImpl.class.getName());
    private final ThreadPrincipalService threadPrincipalService;
    private final ScheduledJob scheduledJob;
    private final User comServerUser;
    private final MultiThreadedScheduledJobCallBack jobCallBack;

    MultiThreadedScheduledJobExecutorImpl(ScheduledJob scheduledJob, TransactionService transactionExecutor, ComServer.LogLevel communicationLogLevel, DeviceCommandExecutor deviceCommandExecutor, ThreadPrincipalService threadPrincipalService, User comServerUser, MultiThreadedScheduledJobCallBack jobCallBack) {
        super(transactionExecutor, communicationLogLevel, deviceCommandExecutor);
        this.threadPrincipalService = threadPrincipalService;
        this.scheduledJob = scheduledJob;
        this.comServerUser = comServerUser;
        this.jobCallBack = jobCallBack;
    }

    @Override
    public void run() {
        setThreadPrinciple();
        try {
            acquireTokenAndPerformSingleJob(scheduledJob);
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, MultiThreadedScheduledJobExecutorImpl.class.getName() + " encountered and ignored an unexpected problem", t);
            t.printStackTrace(System.err);
        } finally {
            jobCallBack.notifyJobExecutorFinished(this);
        }
    }

    private void setThreadPrinciple() {
        threadPrincipalService.set(comServerUser, "MultiThreadedComPort", "Executing", comServerUser.getLocale().orElse(Locale.ENGLISH));
    }

    @Override
    public ConnectionTask getConnectionTask() {
        return scheduledJob.getConnectionTask();
    }

    @Override
    public boolean isExecutingHighPriorityJob() {
        return scheduledJob.isHighPriorityJob();
    }

    @Override
    public boolean isExecutingParallelRootScheduledJob() {
        return scheduledJob != null && scheduledJob instanceof ParallelRootScheduledJob;
    }
}