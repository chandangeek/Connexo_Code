/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

class MultiThreadedScheduledJobExecutor extends ScheduledJobExecutor implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(MultiThreadedScheduledJobExecutor.class.getName());
    private final ThreadPrincipalService threadPrincipalService;
    private final ScheduledJob scheduledJob;
    private final User comServerUser;

    MultiThreadedScheduledJobExecutor(ScheduledJob scheduledJob, TransactionService transactionExecutor, ComServer.LogLevel communicationLogLevel, DeviceCommandExecutor deviceCommandExecutor, ThreadPrincipalService threadPrincipalService, User comServerUser) {
        super(transactionExecutor, communicationLogLevel, deviceCommandExecutor);
        this.threadPrincipalService = threadPrincipalService;
        this.scheduledJob = scheduledJob;
        this.comServerUser = comServerUser;
    }

    @Override
    public void run() {
        this.setThreadPrinciple();
        try {
            acquireTokenAndPerformSingleJob(scheduledJob);
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, MultiThreadedScheduledJobExecutor.class.getName() + " encountered and ignored an unexpected problem", t);
            t.printStackTrace(System.err);
        }
    }

    private void setThreadPrinciple() {
        threadPrincipalService.set(comServerUser, "MultiThreadedComPort", "Executing", comServerUser.getLocale().orElse(Locale.ENGLISH));
    }
}