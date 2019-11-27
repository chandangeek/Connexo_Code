/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.users.User;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.HighPriorityComJob;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provides an implementation for the {@link ScheduledComPort} interface
 * for an {@link OutboundComPort} that supports one connection at a time.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-03 (10:30)
 */
public class SingleThreadedScheduledComPort extends ScheduledComPortImpl {

    private final AtomicBoolean continueRunningAfterInterrupt = new AtomicBoolean(false);
    private SingleThreadedJobScheduler jobScheduler;

    SingleThreadedScheduledComPort(RunningComServer runningComServer, OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider) {
        super(runningComServer, comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
    }

    public SingleThreadedScheduledComPort(RunningComServer runningComServer, OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ThreadFactory threadFactory, ServiceProvider serviceProvider) {
        super(runningComServer, comPort, comServerDAO, deviceCommandExecutor, threadFactory, serviceProvider);
    }

    @Override
    protected void setThreadPrinciple() {
        User comServerUser = getComServerDAO().getComServerUser();
        if (comServerUser != null) {
            getServiceProvider().threadPrincipalService().set(comServerUser, "SingleThreadedComPort", "Executing", comServerUser.getLocale().orElse(Locale.ENGLISH));
        } else {
            getServiceProvider().threadPrincipalService().set(comServerUser, "SingleThreadedComPort", "Executing", Locale.ENGLISH);
        }
    }

    @Override
    protected void doRun() {
        if (continueRunningAfterInterrupt.get()) {
            resetThreadsInterruptedFlag();
            getJobScheduler().executeHighPriorityTasks();
            continueRunningAfterInterrupt.set(false);
        } else {
            goSleepIfWokeUpTooEarly();
            executeTasks();
        }
    }

    @Override
    protected boolean continueRunning() {
        return super.continueRunning() || continueRunningAfterInterrupt.get();
    }

    private boolean resetThreadsInterruptedFlag() {
        return Thread.interrupted(); // Which will read and more important also clear the interrupted flag
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
    public void updateLogLevel(OutboundComPort comPort) {
        setComPort(comPort);
    }

    @Override
    public boolean isExecutingOneOf(List<ComTaskExecution> comTaskExecutions) {
        return getJobScheduler().isExecutingOneOf(comTaskExecutions);
    }

    @Override
    public boolean isConnectedTo(OutboundConnectionTask connectionTask) {
        return getJobScheduler().isConnectedTo(connectionTask);
    }

    @Override
    public Map<Long, Integer> getHighPriorityLoadPerComPortPool() {
        return getJobScheduler().getHighPriorityLoadPerComPortPool();
    }

    @Override
    public void executeWithHighPriority(HighPriorityComJob job) {
        // Schedule the task first
        getJobScheduler().scheduleWithHighPriority(job);
        // Then interrupt the thread but make sure that it keeps running so that it will pick up the task that was scheduled above as the first task
        continueRunningAfterInterrupt.set(true);
        interrupt();
    }

    @Override
    protected SingleThreadedJobScheduler getJobScheduler() {
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
        private SingleThreadedScheduledJobExecutor currentExecutor;

        private SingleThreadedJobScheduler(ComServer.LogLevel logLevel) {
            super();
            this.logLevel = logLevel;
        }

        public boolean isExecutingOneOf(List<ComTaskExecution> comTaskExecutions) {
            return this.currentExecutor != null && this.currentExecutor.isExecutingOneOf(comTaskExecutions);
        }

        public boolean isConnectedTo(OutboundConnectionTask connectionTask) {
            return this.currentExecutor != null && this.currentExecutor.isConnectedTo(connectionTask);
        }

        public Map<Long, Integer> getHighPriorityLoadPerComPortPool() {
            Map<Long, Integer> loadPerComPortPool = new HashMap<>(scheduledJobs.size());
            for (ScheduledJob scheduledJob : this.scheduledJobs) {
                if (scheduledJob.isHighPriorityJob()) {
                    long comPortPoolId = ((JobExecution) scheduledJob).getConnectionTask().getComPortPool().getId();
                    if (loadPerComPortPool.containsKey(comPortPoolId)) {
                        loadPerComPortPool.put(comPortPoolId, loadPerComPortPool.get(comPortPoolId) + 1);
                    } else {
                        loadPerComPortPool.put(comPortPoolId, 1);
                    }
                }
            }
            return loadPerComPortPool;
        }

        @Override
        public void executionStarted(ScheduledJob job) {
            // I know because I kicked the execute method myself
        }

        @Override
        public int scheduleAll(List<ComJob> jobs) {
            scheduledJobs = this.toScheduledJobs(jobs);
            executeScheduledJobs();
            return jobs.size();
        }

        private void executeScheduledJobs() {
            for (ScheduledJob job : this.scheduledJobs) {
                try {
                    LOGGER.info("[" + Thread.currentThread().getName() + "] executing job on SingleThreadedScheduledJobExecutor");
                    this.currentExecutor = new SingleThreadedScheduledJobExecutor(getServiceProvider().transactionService(), this.logLevel, getDeviceCommandExecutor());
                    this.currentExecutor.acquireTokenAndPerformSingleJob(job);
                } finally {
                    this.currentExecutor = null;
                    if (!Thread.currentThread().isInterrupted()) {
                        this.scheduledJobs = new ArrayList<>();
                    }
                }
            }
        }

        public void scheduleWithHighPriority(HighPriorityComJob job) {
            this.scheduledJobs = this.toScheduledJobs(job);
        }

        public void executeHighPriorityTasks() {
            this.executeScheduledJobs();
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

        private List<ScheduledJob> toScheduledJobs(HighPriorityComJob job) {
            List<ScheduledJob> scheduledJobs = new ArrayList<>();
            scheduledJobs.add(newComTaskGroup(job));
            return scheduledJobs;
        }
    }
}