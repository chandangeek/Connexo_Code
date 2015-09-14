package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;

import com.elster.jupiter.users.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private BlockingQueue<ScheduledJob> jobQueue;

    public MultiThreadedScheduledComPort(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider) {
        super(comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
        this.jobQueue = new ArrayBlockingQueue<>(comPort.getNumberOfSimultaneousConnections());
    }

    public MultiThreadedScheduledComPort(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ThreadFactory threadFactory, ServiceProvider serviceProvider) {
        super(comPort, comServerDAO, deviceCommandExecutor, new ComPortThreadFactory(comPort, threadFactory), serviceProvider);
        this.jobQueue = new ArrayBlockingQueue<>(comPort.getNumberOfSimultaneousConnections());
    }

    @Override
    protected void setThreadPrinciple() {
        Optional<User> user = getServiceProvider().userService().findUser("batch executor");
        if (user.isPresent()) {
            getServiceProvider().threadPrincipalService().set(user.get(), "MultiThreadedComPortRunner", "Executing", Locale.ENGLISH);
        }
    }

    @Override
    protected void doStart () {
        this.createJobScheduler();
        super.doStart();
    }

    private void createJobScheduler () {
        int threadPoolSize = this.getComPort().getNumberOfSimultaneousConnections();
        this.jobScheduler = new MultiThreadedJobScheduler(threadPoolSize, this.getThreadFactory());
    }

    @Override
    public void shutdown () {
        this.shutdownJobScheduler();
        super.shutdown();
    }

    @Override
    public void shutdownImmediate () {
        this.shutdownJobScheduler();
        super.shutdownImmediate();
    }

    private void shutdownJobScheduler () {
        this.jobScheduler.shutdown();
    }

    @Override
    protected void doRun () {
        this.executeTasks();
    }

    @Override
    protected JobScheduler getJobScheduler () {
        return this.jobScheduler;
    }

    /**
     * Frees the {@link DeviceCommandExecutionToken} that was reserved
     * for the execution of the specified {@link ComTaskExecution}
     * that apparently was already scheduled for execution by this MultiThreadedScheduledComPort.
     *
     * @param comTaskExecution The ComTaskExecution
     * @param token The DeviceCommandExecutionToken that was reserved for the execution
     */
    protected void alreadyScheduled (ComTaskExecution comTaskExecution, DeviceCommandExecutionToken token) {
        this.getDeviceCommandExecutor().free(token);
        this.alreadyScheduled(comTaskExecution);
    }

    /**
     * Notify interested parties that the {@link ComTaskExecution} was already
     * scheduled for execution by this MultiThreadedScheduledComPort.
     *
     * @param comTaskExecution The ComTaskExecution
     */
    protected void alreadyScheduled(ComTaskExecution comTaskExecution) {
        this.getLogger().alreadyScheduled(this.getThreadName(), comTaskExecution);
    }

    /**
     * Notify interested parties that the {@link ComTaskExecution} could not
     * be scheduled because the queue is full.
     *
     * @param comTaskExecution The ComTaskExecution
     */
    protected void cannotSchedule(ComTaskExecution comTaskExecution) {
        this.getLogger().cannotSchedule(this.getThreadName(), comTaskExecution);
    }

    private final class MultiThreadedJobScheduler implements JobScheduler {
        private ExecutorService executorService;

        private MultiThreadedJobScheduler (int threadPoolSize, ThreadFactory threadFactory) {
            super();
            this.executorService = Executors.newFixedThreadPool(threadPoolSize, threadFactory);
            for (int i = 0; i < threadPoolSize; i++) {
                executorService.submit(
                        new MultiThreadedScheduledJobExecutor(
                                getComPort(),
                                jobQueue,
                                getDeviceCommandExecutor(), getServiceProvider().transactionService(),
                                getServiceProvider().threadPrincipalService(),
                                getServiceProvider().userService()));
            }
        }

        private void shutdown () {
            this.executorService.shutdownNow(); // Not interested in the jobs that have not been picked up
        }

        @Override
        public void scheduleAll(List<ComJob> jobs) {
            List<ScheduledComTaskExecutionGroup> groups = new ArrayList<>(jobs.size());   // At most all jobs will be groups
            for (ComJob job : jobs) {
                if (job.isGroup()) {
                    groups.add(newComTaskGroup(job));
                }
                else {
                    List<ComTaskExecution> scheduledComTasks = job.getComTaskExecutions();
                    ComTaskExecution onlyOne = scheduledComTasks.get(0);
                    this.scheduleNow(onlyOne);
                }
            }
            this.scheduleGroups(groups);
            this.giveTheConsumersSomeSpace();
        }

        /**
         * After we populate the queue, it is recommended to wait a couple of seconds for the workers to fetch and lock the tasks.
         * This way the first tasks aren't fetched again and only non busy tasks are put on the queue.
         * This is necessary even if the queue's put method is blocking because:
         * <ul>
         * <li>In the situation where just enough tasks were queried to fill the queue (i.e. the next put call would block),
         *     the task query will run again and return exactly the same tasks, putting them on the queue but blocking
         *     until one of the threads has picked up a task.
         *     In that case, we are putting a task on the queue that is no longer pending.</li>
         * </ul>
         * <li>In the situation where tasks are actually grouped because the connection task does not support
         *     simultaneous connections, there is likely space left on the queue that will be filled by
         *     subsequent call(s) to the task query but all tasks will be the same.</li>
         * Both situation above are wasting database resources by executing the task query.
         */
        private void giveTheConsumersSomeSpace() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        /**
         * Schedules the {@link ComTaskExecution} with the CompletionService now,
         * i.e. adds it to the list of available {@link ScheduledJob}s
         * so that it can be picked up by one of the delegates.
         * Ignores the ScheduledComTask if it is in fact already scheduled.
         * The latter is possible if the execution of ScheduledComTasks
         * is slower than the {@link com.energyict.mdc.engine.config.ComServer}'s
         * scheduling interpoll delay.
         *
         * @param comTaskExecution The ComTaskExecution
         */
        private void scheduleNow(ComTaskExecution comTaskExecution) {
            try {
                ScheduledComTaskExecutionJob job = newComTaskJob(comTaskExecution);
                try {
                    jobQueue.put(job);
                } catch (InterruptedException e) {
                    Thread.currentThread().isInterrupted();
                }
            }
            catch (RejectedExecutionException e) {
                MultiThreadedScheduledComPort.this.cannotSchedule(comTaskExecution);
            }
        }

        private void scheduleGroups(Collection<ScheduledComTaskExecutionGroup> groups) {
            for (ScheduledComTaskExecutionGroup group : groups) {
                try {
                    try {
                        jobQueue.put(group);
                    } catch (InterruptedException e) {
                        Thread.currentThread().isInterrupted();
                    }
                } catch (RejectedExecutionException e) {
                    group.getComTaskExecutions().forEach(MultiThreadedScheduledComPort.this::cannotSchedule);
                }
            }
        }

    }
}