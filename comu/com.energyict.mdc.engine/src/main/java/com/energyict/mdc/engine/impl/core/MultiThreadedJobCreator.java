/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * My responsibility is to fetch jobs from the BlockingQueue that the ScheduledComPort (producer) fills up
 * and translates them to Jobs that can be handled by my JobExecutors.
 * <p>
 * I can either block by waiting on new jobs to be available on the queue,
 * or by starting a new worker when no free workers are available.
 * <p>
 * I can have max. n JobExecutors as there are simultaneous connections allowed on the ComPort of the ScheduledComPort
 */
public class MultiThreadedJobCreator implements Runnable {

    private final BlockingQueue<ScheduledJobImpl> jobBlockingQueue;
    private final OutboundComPort comPort;
    private final ThreadPrincipalService threadPrincipalService;
    private final ExecutorService executor;
    private final ComServer.LogLevel communicationLogLevel;
    private final TransactionService transactionExecutor;
    private final DeviceCommandExecutor deviceCommandExecutor;
    private final ScheduledComPortImpl.ServiceProvider serviceProvider;
    private final User comServerUser;


    MultiThreadedJobCreator(BlockingQueue<ScheduledJobImpl> jobBlockingQueue, OutboundComPort comPort, DeviceCommandExecutor deviceCommandExecutor, int threadPoolSize, ThreadFactory threadFactory, ScheduledComPortImpl.ServiceProvider serviceProvider, User comServerUser) {
        this.jobBlockingQueue = jobBlockingQueue;
        this.comPort = comPort;
        this.serviceProvider = serviceProvider;
        this.threadPrincipalService = serviceProvider.threadPrincipalService();
        this.transactionExecutor = serviceProvider.transactionService();
        this.comServerUser = comServerUser;
        this.executor = newFixedThreadPoolWithQueueSize(threadPoolSize, threadFactory);
        this.communicationLogLevel = comPort.getComServer().getCommunicationLogLevel();
        this.deviceCommandExecutor = deviceCommandExecutor;
    }

    @Override
    public void run() {

        Thread.currentThread().setName("MultiThreadedJobCreator for " + comPort.getName());
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ScheduledJobImpl scheduledJob = jobBlockingQueue.take();

                if (scheduledJob.getConnectionTask().getNumberOfSimultaneousConnections() > 1) {
                    CountDownLatch start = new CountDownLatch(1); // latch used to start the workers

                    ParallelRootScheduledJob parallelRootScheduledJob = new ParallelRootScheduledJob(((OutboundComPort) scheduledJob.getComPort()),
                            scheduledJob.getComServerDAO(), deviceCommandExecutor, scheduledJob.getConnectionTask(), start, serviceProvider);
                    scheduledJob.getComTaskExecutions().forEach(parallelRootScheduledJob::add);
                    executor.execute(new MultiThreadedScheduledJobExecutor(parallelRootScheduledJob, transactionExecutor, communicationLogLevel, deviceCommandExecutor, threadPrincipalService, comServerUser));
                    for (int i = 1; i < scheduledJob.getConnectionTask().getNumberOfSimultaneousConnections(); i++) { // we start @ 1 because the root also executes groupedDeviceCommands after it finished the population of the queue
                        ParallelWorkerScheduledJob parallelWorkerScheduledJob = new ParallelWorkerScheduledJob(parallelRootScheduledJob, start, threadPrincipalService, comServerUser);
                        executor.execute(parallelWorkerScheduledJob);
                    }

                } else {
                    executor.execute(new MultiThreadedScheduledJobExecutor(scheduledJob, transactionExecutor, communicationLogLevel, deviceCommandExecutor, threadPrincipalService, comServerUser));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private ExecutorService newFixedThreadPoolWithQueueSize(int maxPoolSize, ThreadFactory threadFactory) {
        /*
      This guarantees that the jobs are executed in the order we provided
     */
        boolean FAIRNESS = true;
        return new ThreadPoolExecutor(maxPoolSize, maxPoolSize, // set the core and max pool size equal
                5000L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(1, FAIRNESS), threadFactory, new RejectByBlockingOnQueueHandler());
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