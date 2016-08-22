package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;

import java.util.concurrent.*;

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
    private final UserService userService;
    private final ExecutorService executor;
    private final ComServer.LogLevel communicationLogLevel;
    private final TransactionService transactionExecutor;
    private final DeviceCommandExecutor deviceCommandExecutor;
    private final JobExecution.ServiceProvider serviceProvider;


    MultiThreadedJobCreator(BlockingQueue<ScheduledJobImpl> jobBlockingQueue, OutboundComPort comPort, TransactionService transactionExecutor, DeviceCommandExecutor deviceCommandExecutor, int threadPoolSize, ThreadFactory threadFactory, ThreadPrincipalService threadPrincipalService, UserService userService, JobExecution.ServiceProvider serviceProvider) {
        this.jobBlockingQueue = jobBlockingQueue;
        this.comPort = comPort;
        this.threadPrincipalService = threadPrincipalService;
        this.userService = userService;
        this.executor = newFixedThreadPoolWithQueueSize(threadPoolSize, threadFactory);
        this.communicationLogLevel = comPort.getComServer().getCommunicationLogLevel();
        this.transactionExecutor = transactionExecutor;
        this.deviceCommandExecutor = deviceCommandExecutor;
        this.serviceProvider = serviceProvider;
    }

    @Override
    public void run() {

        Thread.currentThread().setName("MultiThreadedJobCreator for " + comPort.getName());
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ScheduledJobImpl scheduledJob = jobBlockingQueue.take();

                if (scheduledJob.getConnectionTask().getNumberOfSimultaneousConnections() > 1) {
                    CountDownLatch start = new CountDownLatch(1); // latch used to start the workers
                    Semaphore finish = new Semaphore(1);

                    ParallelRootScheduledJob parallelRootScheduledJob = new ParallelRootScheduledJob(((OutboundComPort) scheduledJob.getComPort()),
                            scheduledJob.getComServerDAO(), deviceCommandExecutor, scheduledJob.getConnectionTask(), start, serviceProvider);
                    for (ComTaskExecution comTaskExecution : scheduledJob.getComTaskExecutions()) {
                        parallelRootScheduledJob.add(comTaskExecution);
                    }
                    executor.execute(new MultiThreadedScheduledJobExecutor(parallelRootScheduledJob, transactionExecutor, communicationLogLevel, deviceCommandExecutor, threadPrincipalService, userService));
                    for (int i = 1; i < scheduledJob.getConnectionTask()
                            .getNumberOfSimultaneousConnections(); i++) { // we start @ 1 because the root also executes groupedDeviceCommands after it finished the population of the queue
                        ParallelWorkerScheduledJob parallelWorkerScheduledJob = new ParallelWorkerScheduledJob(parallelRootScheduledJob, start);
                        executor.execute(parallelWorkerScheduledJob);
                    }

                } else {
                    executor.execute(new MultiThreadedScheduledJobExecutor(scheduledJob, transactionExecutor, communicationLogLevel, deviceCommandExecutor, threadPrincipalService, userService));
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
                new ArrayBlockingQueue<Runnable>(1, FAIRNESS), threadFactory, new RejectByBlockingOnQueueHandler());
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