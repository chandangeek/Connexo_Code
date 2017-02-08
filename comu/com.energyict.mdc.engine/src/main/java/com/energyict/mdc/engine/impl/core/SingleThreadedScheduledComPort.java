/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.users.User;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadFactory;

/**
 * Provides an implementation for the {@link ScheduledComPort} interface
 * for an {@link OutboundComPort} that supports one connection at a time.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-03 (10:30)
 */
public class SingleThreadedScheduledComPort extends ScheduledComPortImpl {

    private JobScheduler jobScheduler;

    SingleThreadedScheduledComPort(RunningComServer runningComServer, OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider) {
        super(runningComServer, comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
    }

    public SingleThreadedScheduledComPort(RunningComServer runningComServer, OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ThreadFactory threadFactory, ServiceProvider serviceProvider) {
        super(runningComServer, comPort, comServerDAO, deviceCommandExecutor, threadFactory, serviceProvider);
    }

    @Override
    protected void setThreadPrinciple() {
        User comServerUser = getComServerDAO().getComServerUser();
        getServiceProvider().threadPrincipalService().set(comServerUser, "SingleThreadedComPort", "Executing", comServerUser.getLocale().orElse(Locale.ENGLISH));
    }

    @Override
    public int getThreadCount() {
        return 1;
    }

    @Override
    public int getActiveThreadCount() {
        return (((SingleThreadedJobScheduler) this.getJobScheduler()).isActive() ? 1 : 0);
    }

    @Override
    protected JobScheduler getJobScheduler() {
        if (this.jobScheduler == null) {
            this.jobScheduler = new SingleThreadedJobScheduler(this.getComPort().getComServer().getCommunicationLogLevel());
        }
        return this.jobScheduler;
    }

    /**
     * Organizes a Collection of {@link ComTaskExecution}s
     * into a Collection of ComTaskExecutionGroups.
     */
    private class SingleThreadedJobScheduler implements JobScheduler, ScheduledJobExecutionEventListener {

        private ComServer.LogLevel logLevel;
        private List<ScheduledJob> scheduledJobs = new ArrayList<>();

        private SingleThreadedJobScheduler(ComServer.LogLevel logLevel) {
            super();
            this.logLevel = logLevel;
        }

        @Override
        public void executionStarted(ScheduledJob job) {
            // I know because I kicked the execute method myself
        }

        @Override
        public int scheduleAll(List<ComJob> jobs) {
            scheduledJobs = this.toScheduledJobs(jobs);
            for (ScheduledJob job : scheduledJobs) {
                new SingleThreadedScheduledJobExecutor(getServiceProvider().transactionService(), this.logLevel, getDeviceCommandExecutor()).acquireTokenAndPerformSingleJob(job);
            }
            return jobs.size();
        }

        @Override
        public int getConnectionCount() {
            int count = 0;
            for (ScheduledJob job : scheduledJobs) {
                if (((JobExecution) job).isConnected()) {
                    count++;
                }
            }
            return count;
        }

        public boolean isActive() {
            for (ScheduledJob job : scheduledJobs) {
                if (((JobExecution) job).getExecutionContext() != null) {
                    return true;
                }
            }
            return false;
        }

        private List<ScheduledJob> toScheduledJobs(List<ComJob> jobs) {
            List<ScheduledJob> scheduledJobs = new ArrayList<>();
            for (ComJob job : jobs) {
                scheduledJobs.add(newComTaskGroup(job));
            }
            return scheduledJobs;
        }
    }
}