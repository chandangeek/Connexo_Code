/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.users.User;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;

/**
 * Provides an implementation for the {@link ScheduledComPort} interface
 * for an {@link OutboundComPort} that supports multiple connections at a time.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-03 (10:30)
 */
public class MultiThreadedScheduledComPort extends ScheduledComPortImpl {

    private MultiThreadedJobScheduler jobScheduler;
    /**
     * Will keep track of the jobs to be executed.
     * The purpose of this blockingQueue is that the scheduler will block if no space is left on the queue.
     * From the moment a worker takes a task from the queue, the scheduler will produce more work (if available)
     */
    private BlockingQueue<ScheduledJobImpl> jobQueue;
    private int threadPoolSize;

    MultiThreadedScheduledComPort(RunningComServer runningComServer, OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider) {
        super(runningComServer, comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
    }

    public MultiThreadedScheduledComPort(RunningComServer runningComServer, OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ThreadFactory threadFactory, ServiceProvider serviceProvider) {
        super(runningComServer, comPort, comServerDAO, deviceCommandExecutor, new ComPortThreadFactory(comPort, threadFactory), serviceProvider);
    }

    protected void setComPort(OutboundComPort comPort) {
        if (comPort != getComPort()) {
            if (this.jobScheduler != null) {
                this.jobScheduler.shutdown();
            }
            super.setComPort(comPort);
            this.jobQueue = new ArrayBlockingQueue<>(threadPoolSize = comPort.getNumberOfSimultaneousConnections());
            if (this.jobScheduler != null) {
                this.jobScheduler = new MultiThreadedJobScheduler(threadPoolSize, this.getThreadFactory());
            }
        }
    }

    @Override
    protected void setThreadPrinciple() {
        User comServerUser = getComServerDAO().getComServerUser();
        getServiceProvider().threadPrincipalService().set(comServerUser, "MultiThreadedComPortRunner", "Executing", comServerUser.getLocale().orElse(Locale.ENGLISH));
    }

    @Override
    public int getThreadCount() {
        return threadPoolSize;
    }

    @Override
    public int getActiveThreadCount() {
        if (getStatus() == ServerProcessStatus.STARTED) {
            return jobScheduler.getActiveJobCount();
        }
        return 0;
    }

    @Override
    protected void doStart() {
        this.createJobScheduler();
        super.doStart();
    }

    private void createJobScheduler() {
        this.jobScheduler = new MultiThreadedJobScheduler(threadPoolSize, this.getThreadFactory());
    }

    @Override
    public void shutdown() {
        this.shutdownJobScheduler();
        super.shutdown();
    }

    @Override
    public void shutdownImmediate() {
        this.shutdownJobScheduler();
        super.shutdownImmediate();
    }

    private void shutdownJobScheduler() {
        if (this.jobScheduler != null)
            this.jobScheduler.shutdown();
    }

    @Override
    protected JobScheduler getJobScheduler() {
        return this.jobScheduler;
    }

    /**
     * Notify interested parties that the {@link ComTaskExecution} could not
     * be scheduled because the queue is full.
     *
     * @param comTaskExecution The ComTaskExecution
     */
    private void cannotSchedule(ComTaskExecution comTaskExecution) {
        this.getLogger().cannotSchedule(this.getThreadName(), comTaskExecution);
    }

    private final class MultiThreadedJobScheduler implements JobScheduler {
        // the ExecutorService that deals with the ConsumerJobProducer
        private ExecutorService executorService;

        //TODO what is the purpose of this object?
        private List<Future<?>> jobCompletions = new ArrayList<>();

        private MultiThreadedJobScheduler(int threadPoolSize, ThreadFactory threadFactory) {
            this.executorService = Executors.newFixedThreadPool(1);
            executorService.submit(new MultiThreadedJobCreator(jobQueue, MultiThreadedScheduledComPort.this.getComPort(), getDeviceCommandExecutor(), threadPoolSize, threadFactory, getServiceProvider(), getComServerDAO().getComServerUser()));
        }

        @Override
        public int getConnectionCount() {
            int count = 0;
            for (ScheduledJob job : jobQueue) {
                if (((JobExecution) job).isConnected()) {
                    count++;
                }
            }
            return count;
        }

        public int getActiveJobCount() {
            int active = jobCompletions.size();
            for (Future<?> jobCompletion : jobCompletions) {
                if (jobCompletion.isCancelled() || jobCompletion.isDone()) {
                    active -= 1;
                }
            }
            return active;
        }

        private void shutdown() {
            this.executorService.shutdownNow(); // This will set the interrupted flag on the ConsumerJobProducer
        }

        @Override
        public int scheduleAll(List<ComJob> jobs) {
            List<ScheduledComTaskExecutionGroup> groups = new ArrayList<>(jobs.size());   // At most all jobs will be groups
            for (ComJob job : jobs) {
                groups.add(newComTaskGroup(job));
            }
            this.scheduleGroups(groups);
            giveTheConsumersSomeSpace();
            return -1;
        }

        /**
         * After we populate the queue, it is recommended to wait a couple of seconds for the workers to fetch and lock the tasks.
         * This way the first tasks aren't fetched again and only non busy tasks are put on the queue.
         */
        private void giveTheConsumersSomeSpace() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void scheduleGroups(Collection<ScheduledComTaskExecutionGroup> groups) {
            for (ScheduledComTaskExecutionGroup group : groups) {
                try {
                    try {
                        jobQueue.put(group);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                } catch (RejectedExecutionException e) {
                    group.getComTaskExecutions().forEach(MultiThreadedScheduledComPort.this::cannotSchedule);
                }
            }
        }

    }
}