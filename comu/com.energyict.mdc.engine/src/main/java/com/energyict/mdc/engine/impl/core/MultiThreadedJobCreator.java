/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;
import com.energyict.mdc.common.tasks.PriorityComTaskExecutionLink;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * My responsibility is to fetch jobs from the BlockingQueue that the ScheduledComPort (producer) fills up
 * and translates them to Jobs that can be handled by my JobExecutors.
 * <p>
 * I can either block by waiting on new jobs to be available on the queue,
 * or by starting a new worker when no free workers are available.
 * <p>
 * I can have max. n JobExecutors as there are simultaneous connections allowed on the ComPort of the ScheduledComPort
 */
public class MultiThreadedJobCreator implements Runnable, MultiThreadedScheduledJobCallBack {

    public static final Logger LOGGER = Logger.getLogger(MultiThreadedJobCreator.class.getName());
    private final OutboundComPort comPort;
    private final ThreadPrincipalService threadPrincipalService;
    private final ExecutorService executor;
    private final Map<MultiThreadedScheduledJobExecutor, Future<?>> executors = new ConcurrentHashMap<>();
    private final ComServer.LogLevel communicationLogLevel;
    private final TransactionService transactionExecutor;
    private final DeviceCommandExecutor deviceCommandExecutor;
    private final ScheduledComPortImpl.ServiceProvider serviceProvider;
    private final User comServerUser;
    private BlockingQueue<ScheduledJobImpl> jobBlockingQueue, newJobBlockingQueue;
    private int threadPoolSize, newThreadPoolSize;
    private CountDownLatch updateLatch;


    MultiThreadedJobCreator(BlockingQueue<ScheduledJobImpl> jobBlockingQueue, OutboundComPort comPort, DeviceCommandExecutor deviceCommandExecutor, int threadPoolSize, ThreadFactory threadFactory, ScheduledComPortImpl.ServiceProvider serviceProvider, User comServerUser) {
        this.jobBlockingQueue = jobBlockingQueue;
        this.comPort = comPort;
        this.threadPoolSize = threadPoolSize;
        this.serviceProvider = serviceProvider;
        this.threadPrincipalService = serviceProvider.threadPrincipalService();
        this.transactionExecutor = serviceProvider.transactionService();
        this.comServerUser = comServerUser;
        this.executor = newFixedThreadPoolWithQueueSize(threadPoolSize, threadFactory);
        this.communicationLogLevel = comPort.getComServer().getCommunicationLogLevel();
        this.deviceCommandExecutor = deviceCommandExecutor;
    }

    protected void update(BlockingQueue<ScheduledJobImpl> jobBlockingQueue, int threadPoolSize) {
        updateLatch = new CountDownLatch(1);
        newJobBlockingQueue = jobBlockingQueue;
        newThreadPoolSize = threadPoolSize;
        updateLatch.countDown();
    }

    private void applyChanges() throws InterruptedException {
        if (updateLatch != null) {
            updateLatch.await();
        }
        if (newJobBlockingQueue != null && jobBlockingQueue.size() == 0 && newThreadPoolSize != threadPoolSize) {
            jobBlockingQueue = newJobBlockingQueue;
            threadPoolSize = newThreadPoolSize;
            if (executor instanceof ThreadPoolExecutor) {
                ((ThreadPoolExecutor) executor).setCorePoolSize(threadPoolSize);
                ((ThreadPoolExecutor) executor).setMaximumPoolSize(threadPoolSize);
            }
        }
    }



    @Override
    public void run() {

        Thread.currentThread().setName("MultiThreadedJobCreator for " + comPort.getName());
        try {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    applyChanges();
                    ScheduledJobImpl scheduledJob = jobBlockingQueue.take();
                    executeScheduledJob(scheduledJob);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private void executeScheduledJob(ScheduledJobImpl scheduledJob) {
        if (scheduledJob.getConnectionTask().getNumberOfSimultaneousConnections() > 1) {
            scheduleParallelJob(scheduledJob);
        } else {
            scheduleSingleJob(scheduledJob);
        }
    }

    private void scheduleParallelJob(ScheduledJobImpl scheduledJob) {
        CountDownLatch start = new CountDownLatch(1); // latch used to start the workers

        ParallelRootScheduledJob parallelRootScheduledJob = new ParallelRootScheduledJob(((OutboundComPort) scheduledJob.getComPort()),
                scheduledJob.getComServerDAO(), deviceCommandExecutor, scheduledJob.getConnectionTask(), start, serviceProvider);
        scheduledJob.getComTaskExecutions().forEach(parallelRootScheduledJob::add);
        scheduleSingleJob(parallelRootScheduledJob);
        for (int i = 1; i < scheduledJob.getConnectionTask()
                .getNumberOfSimultaneousConnections(); i++) { // we start @ 1 because the root also executes groupedDeviceCommands after it finished the population of the queue
            ParallelWorkerScheduledJob parallelWorkerScheduledJob = new ParallelWorkerScheduledJob(parallelRootScheduledJob, start, threadPrincipalService, comServerUser, this);
            executors.put(parallelWorkerScheduledJob, executor.submit(parallelWorkerScheduledJob));
        }
    }

    private void scheduleSingleJob(ScheduledJobImpl scheduledJob) {
        MultiThreadedScheduledJobExecutor newExecutor = new MultiThreadedScheduledJobExecutorImpl(scheduledJob, transactionExecutor, communicationLogLevel, deviceCommandExecutor, threadPrincipalService, comServerUser, this);
        executors.put(newExecutor, executor.submit(newExecutor));
    }

    public boolean isConnectedTo(OutboundConnectionTask connectionTask) {
        synchronized (executors) {
            for (MultiThreadedScheduledJobExecutor jobExecutor : executors.keySet()) {
                if (jobExecutor.isConnectedTo(connectionTask)) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean isExecutingOneOf(List<ComTaskExecution> comTaskExecutions) {
        synchronized (executors) {
            for (MultiThreadedScheduledJobExecutor executor : executors.keySet()) {
                if (executor.isExecutingOneOf(comTaskExecutions)) {
                    return true;
                }
            }
            return false;
        }
    }

    public Map<Long, Integer> getHighPriorityLoadPerComPortPool() {
        synchronized (executors) {
            Map<Long, Integer> loadPerComPortPool = new HashMap<>(executors.size());
            int i = 1;
            for (MultiThreadedScheduledJobExecutor executor : executors.keySet()) {
                boolean executesHighPrio = false;
                if (executor.isExecutingHighPriorityJob()) {
                    executesHighPrio = true;
                    long comPortPoolId = executor.getConnectionTask().getComPortPool().getId();
                    if (loadPerComPortPool.containsKey(comPortPoolId)) {
                        loadPerComPortPool.put(comPortPoolId, loadPerComPortPool.get(comPortPoolId) + 1);
                    } else {
                        loadPerComPortPool.put(comPortPoolId, 1);
                    }
                    LOGGER.info("[high-prio] load for pool " + comPortPoolId + ": " + loadPerComPortPool.get(comPortPoolId));
                }
            }
            return loadPerComPortPool;
        }
    }

    private ExecutorService newFixedThreadPoolWithQueueSize(int maxPoolSize, ThreadFactory threadFactory) {
        /**
         * This guarantees that the jobs are executed in the order we provided
        */
        boolean FAIRNESS = true;
        return new ThreadPoolExecutor(maxPoolSize, maxPoolSize, // set the core and max pool size equal
                5000L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(1, FAIRNESS), threadFactory, new RejectByBlockingOnQueueHandler());
    }

    public int getActiveJobCount() {
        synchronized (this.executors) {
            int active = executors.size();
            for (Future<?> jobCompletion : executors.values()) {
                if (jobCompletion.isCancelled() || jobCompletion.isDone()) {
                    active -= 1;
                }
            }
            return active;
        }
    }

    public void executeWithHighPriority(HighPriorityComTaskExecutionGroup scheduledJob) {
        MultiThreadedScheduledJobExecutor interruptCandidate;
        synchronized (this.executors) {
            interruptCandidate = findLeastHarmfulExecutorForInterrupt(scheduledJob);
        }
        interruptLeastHarmfulExecutor(scheduledJob, interruptCandidate); //Outside of the synchronized block
        if (scheduledJob.getConnectionTask().getNumberOfSimultaneousConnections() > 1) {
            scheduleParallelJob(scheduledJob);
        } else {
            scheduleSingleJob(scheduledJob);
        }
    }

    private void scheduleParallelJob(HighPriorityComTaskExecutionGroup scheduledJob) {
        CountDownLatch start = new CountDownLatch(1); // latch used to start the workers
        HighPriorityParallelRootScheduledJob parallelRootScheduledJob = new HighPriorityParallelRootScheduledJob(((OutboundComPort) scheduledJob.getComPort()),
                scheduledJob.getComServerDAO(), deviceCommandExecutor, scheduledJob.getConnectionTask(), start, serviceProvider);
        for (PriorityComTaskExecutionLink comTaskExecution : scheduledJob.getPriorityComTaskExecutionLinks()) {
            parallelRootScheduledJob.add(comTaskExecution);
        }
        MultiThreadedScheduledJobExecutorImpl newExecutor = new MultiThreadedScheduledJobExecutorImpl(parallelRootScheduledJob, transactionExecutor, communicationLogLevel, deviceCommandExecutor, threadPrincipalService, comServerUser, this);
        executors.put(newExecutor, executor.submit(newExecutor));
        for (int i = 1; i < scheduledJob.getConnectionTask()
                .getNumberOfSimultaneousConnections(); i++) { // we start @ 1 because the root also executes groupedDeviceCommands after it finished the population of the queue
            ParallelWorkerScheduledJob parallelWorkerScheduledJob = new ParallelWorkerScheduledJob(parallelRootScheduledJob, start, threadPrincipalService, comServerUser, this);
            executors.put(parallelWorkerScheduledJob, executor.submit(parallelWorkerScheduledJob));
        }
    }

    private MultiThreadedScheduledJobExecutor findLeastHarmfulExecutorForInterrupt(HighPriorityComTaskExecutionGroup job) {
        MultiThreadedScheduledJobExecutor interruptCandidate;
        if (!isConnectedTo(job.getConnectionTask())) {
            if (this.executors.size() < this.threadPoolSize) {
                return null; // No need to interrupt any executor, as a new one can be added
            } else {
                List<MultiThreadedScheduledJobExecutor> interruptCandidates = new ArrayList<>(this.executors.keySet());
                Collections.sort(interruptCandidates, new MultiThreadedScheduledJobExecutorForInterruptionComparator(job));
                interruptCandidate = interruptCandidates.get(0);
            }
        } else {
            if (job.getConnectionTask().getNumberOfSimultaneousConnections() == 1) {
                // We are already busy communicating to the connection, which doesn't allow simultaneous connections
                // In this case, we should interrupt the MultiThreadedScheduledJobExecutor who is performing the communication to the connection
                interruptCandidate = getMultiThreadedScheduledJobExecutorConnectedTo(job);
            } else {
                // We are already  busy communicating to the connection, which does allow simultaneous connections
                // In this case, we should interrupt one of the ParallelWorkerScheduledJob threads if necessary
                List<ParallelWorkerScheduledJob> parallelWorkerScheduledJobsInterruptCandidates = findParallelWorkerScheduledJobsConnectedTo(job.getConnectionTask());
                if (parallelWorkerScheduledJobsInterruptCandidates.size() < (job.getConnectionTask().getNumberOfSimultaneousConnections() - 1) && this.executors.size() < this.threadPoolSize) {
                    // Not yet at maximum number of simultaneous connections in use and not yet at maximum number of executors
                    // In this case, no need to interrupt a thread
                    return null;
                } else if (parallelWorkerScheduledJobsInterruptCandidates.isEmpty()) {
                    interruptCandidate = getMultiThreadedScheduledJobExecutorConnectedTo(job);
                } else {
                    Collections.sort(parallelWorkerScheduledJobsInterruptCandidates, new MultiThreadedScheduledJobExecutorForInterruptionComparator(job));
                    interruptCandidate = parallelWorkerScheduledJobsInterruptCandidates.get(0);
                }
            }
        }
        return interruptCandidate;
    }

    private void interruptLeastHarmfulExecutor(HighPriorityComTaskExecutionGroup job, MultiThreadedScheduledJobExecutor interruptCandidate) {
        if (interruptCandidate != null) {
            this.executors.get(interruptCandidate).cancel(true);
            this.executors.remove(interruptCandidate);
        }
    }

    private MultiThreadedScheduledJobExecutor getMultiThreadedScheduledJobExecutorConnectedTo(HighPriorityComTaskExecutionGroup job) {
        // Should be already synchronized on executors
        for (MultiThreadedScheduledJobExecutor jobExecutor : this.executors.keySet()) {
            if (jobExecutor.isConnectedTo(job.getConnectionTask())) {
                return jobExecutor;
            }
        }
        return null;
    }

    private List<ParallelWorkerScheduledJob> findParallelWorkerScheduledJobsConnectedTo(OutboundConnectionTask connectionTask) {
        // Should be already synchronized on executors
        List<ParallelWorkerScheduledJob> jobExecutors = new ArrayList<>(threadPoolSize);
        for (MultiThreadedScheduledJobExecutor jobExecutor : this.executors.keySet()) {
            if (jobExecutor instanceof ParallelWorkerScheduledJob && jobExecutor.isConnectedTo(connectionTask)) {
                jobExecutors.add((ParallelWorkerScheduledJob) jobExecutor);
            }
        }
        return jobExecutors;
    }

    @Override
    public void notifyJobExecutorFinished(MultiThreadedScheduledJobExecutorImpl jobExecutor) {
        executors.remove(jobExecutor);
    }

    @Override
    public void notifyJobExecutorFinished(ParallelWorkerScheduledJob parallelWorkerScheduledJob) {
        executors.remove(parallelWorkerScheduledJob);
    }

    private static class MultiThreadedScheduledJobExecutorForInterruptionComparator implements Comparator<MultiThreadedScheduledJobExecutor> {
        HighPriorityComTaskExecutionGroup job;

        private MultiThreadedScheduledJobExecutorForInterruptionComparator(HighPriorityComTaskExecutionGroup job) {
            super();
            this.job = job;
        }

        @Override
        public int compare(MultiThreadedScheduledJobExecutor first, MultiThreadedScheduledJobExecutor second) {
            // Sort based whether or not busy with the execution of a high priority job, these should preferably not be interrupted
            if (first.isExecutingHighPriorityJob() && second.isExecutingHighPriorityJob()) {
                return 0;
            } else if (first.isExecutingHighPriorityJob()) {
                return 1;
            } else if (second.isExecutingHighPriorityJob()) {
                return -1;
            }

            // Sort based whether or not busy with the execution of a ParallelRootScheduledJob, these should preferably not be interrupted
            if (first.isExecutingParallelRootScheduledJob() && !second.isExecutingParallelRootScheduledJob()) {
                return 1;
            } else if (!first.isExecutingParallelRootScheduledJob() && second.isExecutingParallelRootScheduledJob()) {
                return -1;
            }

            if (first.isExecutingOneOf(this.job.getComTaskExecutions())) {
                // The second cannot be executing the same task because of the locking mechanism
                return -1;
            } else if (second.isExecutingOneOf(this.job.getComTaskExecutions())) {
                return 1;
            } else {
                /* Sort such that smaller tasks get interrupted first. */
                return this.compareOnNumberOfTasks(first, second);
            }
        }

        private int compareOnNumberOfTasks(MultiThreadedScheduledJobExecutor first, MultiThreadedScheduledJobExecutor second) {
            return Integer.compare(first.getNumberOfTasks(), second.getNumberOfTasks());
        }
    }

    private class RejectByBlockingOnQueueHandler implements RejectedExecutionHandler {
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            try {
                executor.getQueue().put(r);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}