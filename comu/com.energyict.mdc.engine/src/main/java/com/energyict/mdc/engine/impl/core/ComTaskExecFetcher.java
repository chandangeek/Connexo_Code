/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.engine.EngineService;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class ComTaskExecFetcher implements Runnable, ServerProcess {
    private static final Logger LOGGER = Logger.getLogger(ComTaskExecFetcher.class.getName());
    private final Map<Long, ScheduledComPort> scheduledComPorts;
    private final Map<Long, List<? extends ComPort>> comPortsComPortPoolBelongsTo;
    private final ThreadFactory threadFactory;
    private final ComServerDAO comServerDAO;
    private final ComServer comServer;
    private final RunningComServerImpl.ServiceProvider serviceProvider;
    private List<ComTaskExecution> comTaskExecutions;
    private volatile ServerProcessStatus status = ServerProcessStatus.SHUTDOWN;
    private AtomicBoolean continueRunning;
    private Thread self;
    private Duration delta;
    private long waitTime;
    private long limitNrOfEntriesToReturn;
    private boolean prefetchBalanced;
    private boolean prefetchEnabled;
    private Map<Long, Integer> balancingMap = new HashMap<>();


    public ComTaskExecFetcher(Map<Long, ScheduledComPort> scheduledComPorts,
                              Map<Long, List<? extends ComPort>> comPortsComPortPoolBelongsTo,
                              ThreadFactory threadFactory, ComServerDAO comServerDAO, ComServer comServer,
                              RunningComServerImpl.ServiceProvider serviceProvider) {
        this.scheduledComPorts = scheduledComPorts;
        this.comPortsComPortPoolBelongsTo = comPortsComPortPoolBelongsTo;
        this.threadFactory = threadFactory;
        this.comServerDAO = comServerDAO;
        this.comServer = comServer;
        this.serviceProvider = serviceProvider;
        initialise(serviceProvider.engineService());
    }

    private void initialise(EngineService engineService) {
        this.delta = Duration.of(engineService.getPrefetchComTaskTimeDelta(), ChronoUnit.SECONDS);
        this.waitTime = Duration.of(engineService.getPrefetchComTaskDelay(), ChronoUnit.SECONDS).toMillis();
        this.limitNrOfEntriesToReturn = engineService.getPrefetchComTaskLimit();
        this.prefetchBalanced = engineService.isPrefetchBalanced();
        this.prefetchEnabled = engineService.isPrefetchEnabled();
    }

    @Override
    public void run() {
        while (continueRunning.get() && !Thread.currentThread().isInterrupted()) {
            try {
                LOGGER.info("Prefetch: " + comServer.getName() + ": Start Prefetch");
                processData();
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        this.status = ServerProcessStatus.SHUTDOWN;
    }

    private void processData() {
        long skip = 0;
        boolean continueFetching;
        do {
            continueFetching = false;
            comTaskExecutions = getComTaskExecutions(skip, limitNrOfEntriesToReturn);
            if (comTaskExecutions != null && !comTaskExecutions.isEmpty()) {
                continueFetching = limitNrOfEntriesToReturn > 0;
                Iterator<ComTaskExecution> itr = comTaskExecutions.iterator();
                while (itr.hasNext()) {
                    publishTask(itr.next());
                    itr.remove();
                }
                skip += limitNrOfEntriesToReturn;
            } else {
                LOGGER.info("Prefetch: " + comServer.getName() + ": No data to fetch!");
            }
        } while (continueFetching);
    }

    private List<ComTaskExecution> getComTaskExecutions(long skip, long limit) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<ComTaskExecution> tasks = comServerDAO.findExecutableOutboundComTasks(comServer, delta, limit, skip);
        stopwatch.stop();
        LOGGER.info("Prefetch: " + comServer.getName() + ":  fetchDataTime: " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms");
        LOGGER.info("Prefetch: " + comServer.getName() + ": fetchDataCount: " + tasks.size());
        return tasks;
    }

    private void publishTask(ComTaskExecution comTaskExecution) {
        SimpleEntry<Long, List<? extends ComPort>> comPorts = comTaskExecution.getConnectionTask()
                .map(conTask -> new SimpleEntry<Long, List<? extends ComPort>>(conTask.getComPortPool().getId(), comPortsComPortPoolBelongsTo.get(conTask.getComPortPool().getId())))
                .filter(entry -> entry.getValue() != null).orElseGet(() -> new SimpleEntry<>(null, Lists.newArrayList()));
        if (prefetchBalanced) {
            publishBalanced(comTaskExecution, comPorts);
        } else {
            publishUnbalanced(comTaskExecution, comPorts.getValue());
        }
    }

    private void publishUnbalanced(ComTaskExecution comTaskExecution, List<? extends ComPort> comPorts) {
        comPorts.stream().map(comPort -> scheduledComPorts.get(comPort.getId()))
                .filter(Objects::nonNull)
                .forEach(scheduledComPort -> scheduledComPort.addTaskToExecute(comTaskExecution));
    }

    private void publishBalanced(ComTaskExecution comTaskExecution, SimpleEntry<Long, List<? extends ComPort>> comPorts) {
        Optional.ofNullable(getComPortToPublish(comPorts))
                .map(comPort -> scheduledComPorts.get(comPort.getId()))
                .ifPresent(scheduledComPort -> scheduledComPort.addTaskToExecute(comTaskExecution));
    }

    private ComPort getComPortToPublish(SimpleEntry<Long, List<? extends ComPort>> comPorts) {
        return Optional.ofNullable(comPorts.getKey())
                .map(cpp -> getBalancedIndex(cpp, comPorts.getValue().size()))
                .filter(index -> index >= 0)
                .map(index -> comPorts.getValue().get(index))
                .orElse(null);
    }

    private int getBalancedIndex(Long key, int portsCount) {
        int index = -1;
        if (portsCount > 0) {
            index = balancingMap.getOrDefault(key, -1) + 1;
            if (index >= portsCount) {
                index = 0;
            }
            balancingMap.put(key, index);
        }
        return index;
    }

    @Override
    public ServerProcessStatus getStatus() {
        return status;
    }

    @Override
    public void start() {
        if (prefetchEnabled) {
            status = ServerProcessStatus.STARTING;
            continueRunning = new AtomicBoolean(true);
            self = threadFactory.newThread(this);
            self.setName("ComTaskExecFetcher for " + comServer.getName());
            self.start();
            status = ServerProcessStatus.STARTED;
        }
    }

    @Override
    public void shutdown() {
        doShutdown();
    }

    @Override
    public void shutdownImmediate() {
        doShutdown();
    }

    private void doShutdown() {
        if (prefetchEnabled) {
            status = ServerProcessStatus.SHUTTINGDOWN;
            continueRunning.set(false);
            self.interrupt();   // in case the thread was sleeping between detecting changes
        }
    }
}
