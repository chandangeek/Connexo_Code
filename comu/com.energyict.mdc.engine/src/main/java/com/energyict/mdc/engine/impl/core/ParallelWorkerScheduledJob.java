/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;

public class ParallelWorkerScheduledJob extends ScheduledComTaskExecutionGroup implements Runnable {

    private final ParallelRootScheduledJob parallelRootScheduledJob;
    private final CountDownLatch start;
    private final ThreadPrincipalService threadPrincipalService;
    private final User comServerUser;

    private Boolean connectionEstablished = null;

    public ParallelWorkerScheduledJob(ParallelRootScheduledJob parallelRootScheduledJob, CountDownLatch start, ThreadPrincipalService threadPrincipalService, User comServerUser) {
        super(((OutboundComPort) parallelRootScheduledJob.getComPort()), parallelRootScheduledJob.getComServerDAO(), parallelRootScheduledJob.getDeviceCommandExecutor(), parallelRootScheduledJob.getConnectionTask(), parallelRootScheduledJob.getServiceProvider());
        this.threadPrincipalService = threadPrincipalService;
        this.comServerUser = comServerUser;
        this.parallelRootScheduledJob = parallelRootScheduledJob;
        this.start = start;
    }

    @Override
    public void run() {
        this.execute();
    }

    @Override
    public void execute() {
        Thread.currentThread().setName("ComPort schedule worker for " + getComPort().getName());
        this.setThreadPrinciple();

        /*
        1/ Connect to the device
        2/ loop and execute until you don't get anything anymore
        3/ store stuff if required
        * */

        try {
            this.start.await(); // wait until the parallelRoot has finished populating the queue

            GroupedDeviceCommand groupedDeviceCommand = parallelRootScheduledJob.next();
            while (groupedDeviceCommand != null) {
                boolean success = false;
                try {
                    checkForConnection();
                    groupedDeviceCommand.updateComChannelForComCommands(getExecutionContext().getComPortRelatedComChannel());
                    groupedDeviceCommand.performAfterConnectionSetup(getExecutionContext());
                    success = true;
                } finally {
                    if (success) {
                        groupedDeviceCommand = parallelRootScheduledJob.next();
                        if (groupedDeviceCommand != null) {
                            parallelRootScheduledJob.completedASingleJob(); // do a countdown
                        } // else the countdown will happen the executionContext is delivered
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            try {
                this.completeConnection();
            } finally {
                this.closeConnection();
                parallelRootScheduledJob.complete(this);
            }
        }
    }

    private boolean checkForConnection() {
        if (connectionEstablished == null) {
            createExecutionContext(false);
            connectionEstablished = this.establishConnectionFor(this.getComPort());
        }
        return connectionEstablished;
    }

    private void setThreadPrinciple() {
        threadPrincipalService.set(comServerUser, "MultiThreadedComPort", "Executing", comServerUser.getLocale().orElse(Locale.ENGLISH));
    }
}