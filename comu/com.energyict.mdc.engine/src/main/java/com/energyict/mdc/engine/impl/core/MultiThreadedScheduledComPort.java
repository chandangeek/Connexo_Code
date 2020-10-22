/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.users.User;
import com.energyict.mdc.common.comserver.HighPriorityComJob;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;
import com.energyict.mdc.common.tasks.ProtocolTask;
import com.energyict.mdc.engine.impl.OfflineDeviceForComTaskGroup;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.commands.store.core.DeviceProtocolCommandCreator;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private volatile BlockingQueue<ScheduledJobImpl> jobQueue;
    private int threadPoolSize;
    private Semaphore mutex = new Semaphore(1);

    MultiThreadedScheduledComPort(RunningComServer runningComServer, OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider) {
        super(runningComServer, comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
    }

    public MultiThreadedScheduledComPort(RunningComServer runningComServer, OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ThreadFactory threadFactory, ServiceProvider serviceProvider) {
        super(runningComServer, comPort, comServerDAO, deviceCommandExecutor, new ComPortThreadFactory(comPort, threadFactory), serviceProvider);
    }

    private void applyNewChanges() {
        if (mutex.tryAcquire()) {
            try {
                if (threadPoolSize != getComPort().getNumberOfSimultaneousConnections()) {
                    threadPoolSize = getComPort().getNumberOfSimultaneousConnections();
                    BlockingQueue oldQueue = jobQueue;
                    jobQueue = new ArrayBlockingQueue<>(threadPoolSize);
                    jobScheduler.multiThreadedJobCreator.update(jobQueue, threadPoolSize);
                    oldQueue.put(new ChangeQueueSignal());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                mutex.release();
            }
        }
    }

    @Override
    protected void setThreadPrinciple() {
        User comServerUser = getComServerDAO().getComServerUser();
        getServiceProvider().threadPrincipalService().set(comServerUser, "MultiThreadedComPortRunner", "Executing", comServerUser.getLocale().orElse(Locale.ENGLISH));
    }

    protected void setComPort(OutboundComPort comPort) {
        if (comPort != getComPort()) {
            if (jobScheduler != null) {
                jobScheduler.shutdown();
            }
            super.setComPort(comPort);
            if (comPort.getNumberOfSimultaneousConnections() != threadPoolSize) {
                threadPoolSize = comPort.getNumberOfSimultaneousConnections();
                jobQueue = new ArrayBlockingQueue<>(threadPoolSize);
            }
            if (jobScheduler != null) {
                createJobScheduler();
            }
        }
    }

    @Override
    protected void doStart() {
        this.createJobScheduler();
        super.doStart();
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

    @Override
    public void reload(OutboundComPort comPort) {
        try {
            mutex.acquire();
            super.setComPort(comPort);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
    }

    @Override
    protected void doRun() {
        goSleepIfWokeUpTooEarly();
        executeTasks();
        applyNewChanges();
    }

    @Override
    protected JobScheduler getJobScheduler() {
        return this.jobScheduler;
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
    public void updateLogLevel(OutboundComPort comPort) {
        setComPort(comPort);
    }

    @Override
    public boolean isExecutingOneOf(List<ComTaskExecution> comTaskExecutions) {
        return jobScheduler.isExecutingOneOf(comTaskExecutions);
    }

    @Override
    public boolean isConnectedTo(OutboundConnectionTask connectionTask) {
        return jobScheduler.isConnectedTo(connectionTask);
    }

    @Override
    public Map<Long, Integer> getHighPriorityLoadPerComPortPool() {
        if (getStatus() == ServerProcessStatus.STARTED) {
            return jobScheduler.getHighPriorityLoadPerComPortPool();
        }
        return new HashMap<>(0);
    }

    @Override
    public void executeWithHighPriority(HighPriorityComJob job) {
        jobScheduler.executeWithHighPriority(job);
    }

    private void createJobScheduler() {
        this.jobScheduler = new MultiThreadedJobScheduler(threadPoolSize, this.getThreadFactory());
    }

    private void shutdownJobScheduler() {
        if (this.jobScheduler != null) {
            this.jobScheduler.shutdown();
        }
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
        private MultiThreadedJobCreator multiThreadedJobCreator;

        //TODO what is the purpose of this object?
        private List<Future<?>> jobCompletions = new ArrayList<>();

        private MultiThreadedJobScheduler(int threadPoolSize, ThreadFactory threadFactory) {
            this.executorService = Executors.newFixedThreadPool(1);
            multiThreadedJobCreator = new MultiThreadedJobCreator(jobQueue, MultiThreadedScheduledComPort.this.getComPort(), getDeviceCommandExecutor(), threadPoolSize, threadFactory, getServiceProvider(), getComServerDAO().getComServerUser());
            executorService.submit(multiThreadedJobCreator);
        }

        public int getActiveJobCount() {
            if (multiThreadedJobCreator == null) {
                return 0;
            }
            return multiThreadedJobCreator.getActiveJobCount();
        }

        private void shutdown() {
            this.executorService.shutdownNow(); // This will set the interrupted flag on the ConsumerJobProducer
        }

        @Override
        public int scheduleAll(List<ComJob> jobs) {
            long start = System.currentTimeMillis();
            List<ScheduledComTaskExecutionGroup> groups = new ArrayList<>(jobs.size());   // At most all jobs will be groups
            for (ComJob job : jobs) {
                for (Map.Entry<ExecutionType, List<ComTaskExecution>> entry : getSplitedComTaskExecutionsLists(job.getComTaskExecutions()).entrySet()) {
                    groups.add(newComTaskGroup(job, entry.getValue()));
                }
            }
            this.scheduleGroups(groups);
            LOGGER.warning("perf - Finished scheduleAll, " + (System.currentTimeMillis() - start));
            giveTheConsumersSomeSpace();
            return -1;
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
            LOGGER.warning("perf - nb of free connections: " + (threadPoolSize - getActiveJobCount()) + " enqueued: " + getDeviceCommandExecutor().getCurrentSize());
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

        public boolean isExecutingOneOf(List<ComTaskExecution> comTaskExecutions) {
            return multiThreadedJobCreator.isExecutingOneOf(comTaskExecutions);
        }

        public boolean isConnectedTo(OutboundConnectionTask connectionTask) {
            return multiThreadedJobCreator.isConnectedTo(connectionTask);
        }

        public Map<Long, Integer> getHighPriorityLoadPerComPortPool() {
            return multiThreadedJobCreator.getHighPriorityLoadPerComPortPool();
        }

        public void executeWithHighPriority(HighPriorityComJob job) {
            multiThreadedJobCreator.executeWithHighPriority(newComTaskGroup(job));
        }

        private Map<ExecutionType, List<ComTaskExecution>> getSplitedComTaskExecutionsLists(List<ComTaskExecution> comTaskExecutions) {
            final OfflineDeviceForComTaskGroup offlineDeviceForComTaskGroup = new OfflineDeviceForComTaskGroup(comTaskExecutions);
            Map<ExecutionType, List<ComTaskExecution>> resultMap = new HashMap<>();
            for (ComTaskExecution comTaskExecution : comTaskExecutions) {
                DeviceIdentifier deviceIdentifier = getServiceProvider().identificationService()
                        .createDeviceIdentifierForAlreadyKnownDevice(comTaskExecution.getDevice().getId(), comTaskExecution.getDevice().getmRID());
                OfflineDevice offlineDevice = getComServerDAO().findOfflineDevice(deviceIdentifier, offlineDeviceForComTaskGroup).get();
                DeviceProtocolPluggableClass deviceProtocolPluggableClass = offlineDevice.getDeviceProtocolPluggableClass();
                DeviceProtocol deviceProtocol = deviceProtocolPluggableClass.getDeviceProtocol();
                CommandRoot commandRoot = new ParallelRootScheduledJob(null, null, null, null, null, getServiceProvider()).initCommandRoot();
                if (DeviceProtocolCommandCreator.hasCommandsToExecute(new GroupedDeviceCommand(commandRoot, offlineDevice, deviceProtocol, null),
                        generateProtocolTaskList(comTaskExecution), comTaskExecution)) {
                    resultMap.computeIfAbsent(ExecutionType.TO_EXECUTE, key -> new ArrayList<>()).add(comTaskExecution);
                } else {
                    resultMap.computeIfAbsent(ExecutionType.NOT_EXECUTE, key -> new ArrayList<>()).add(comTaskExecution);
                }
            }
            return resultMap;
        }

        List<ProtocolTask> generateProtocolTaskList(ComTaskExecution comTaskExecution) {
            return generateProtocolTaskList(comTaskExecution.getComTask())
                    .collect(Collectors.toList());
        }

        private Stream<ProtocolTask> generateProtocolTaskList(ComTask comTask) {
            List<ProtocolTask> protocolTasks = new ArrayList<>(comTask.getProtocolTasks()); // Copies the unmodifiable list
            Collections.sort(protocolTasks, JobExecution.BasicCheckTasks.FIRST);
            return protocolTasks.stream();
        }

    }

}