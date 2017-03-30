/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.ComSessionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class ParallelRootScheduledJob extends ScheduledComTaskExecutionGroup {

    private final BlockingQueue<GroupedDeviceCommand> groupedDeviceCommands = new LinkedBlockingQueue<>();
    private final CountDownLatch start;
    private final Map<ParallelWorkerScheduledJob, ExecutionContext> completedWorkers = new ConcurrentHashMap<>();
    private CountDownLatch finish = new CountDownLatch(0); // safety measure

    public ParallelRootScheduledJob(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ScheduledConnectionTask connectionTask, CountDownLatch start, ServiceProvider serviceProvider) {
        super(comPort, comServerDAO, deviceCommandExecutor, connectionTask, serviceProvider);
        this.start = start;
    }

    @Override
    public void execute() {
        Thread.currentThread().setName("ComPort ParallelRoot ScheduledJob for " + getComPort().getName());
        try {
            boolean connectionEstablished;
            this.createExecutionContext();
            commandRoot = this.prepareAll(getComTaskExecutions());

            if (!commandRoot.hasGeneralSetupErrorOccurred()) {
                connectionEstablished = this.establishConnectionFor(this.getComPort());
                if (connectionEstablished) {

                    for (GroupedDeviceCommand groupedDeviceCommand : commandRoot) {
                        groupedDeviceCommands.add(groupedDeviceCommand);
                    }
                    finish = new CountDownLatch(groupedDeviceCommands.size());

                    start.countDown(); // notify the workers that they can pull from the queue

                    GroupedDeviceCommand groupedDeviceCommand;
                    while ((groupedDeviceCommand = next()) != null) {
                        groupedDeviceCommand.performAfterConnectionSetup(getExecutionContext());
                        finish.countDown();
                    }
                } else {
                    commandRoot.execute(false);
                }
            } else {
                // let the commandRoot properly handle this
                commandRoot.execute(false);
            }

        } finally {
            start.countDown(); // safety measures

            try {
                this.completeConnection();
            } finally {
                this.closeConnection();
            }
        }

        try {
            finish.await(); //wait until everyone is done and then store the damn thing

            ComSessionBuilder rootComSessionBuilder = getExecutionContext().getComSessionBuilder();
            for (ExecutionContext executionContext : completedWorkers.values()) {
                ComSessionBuilder workerBuilder = executionContext.getComSessionBuilder();
                combineJournals(rootComSessionBuilder, workerBuilder);
                combineStatistics(rootComSessionBuilder, workerBuilder);
                combineCollectedData(executionContext);

            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void combineJournals(ComSessionBuilder rootComSessionBuilder, ComSessionBuilder workerBuilder) {
        for (ComTaskExecutionSessionBuilder comTaskExecutionSessionBuilder : workerBuilder.getComTaskExecutionSessionBuilders()) {
            rootComSessionBuilder.addComTaskExecutionSession(comTaskExecutionSessionBuilder);
        }
        for (ComSessionJournalEntry comSessionJournalEntryBuilder : workerBuilder.getJournalEntries()) {
            rootComSessionBuilder.addJournalEntry(comSessionJournalEntryBuilder);
        }
    }

    private void combineStatistics(ComSessionBuilder rootComSessionBuilder, ComSessionBuilder workerBuilder) {
        rootComSessionBuilder.addReceivedBytes(workerBuilder.getReceivedBytes());
        rootComSessionBuilder.addSentBytes(workerBuilder.getSentBytes());
        rootComSessionBuilder.addReceivedPackets(workerBuilder.getReceivedPackets());
        rootComSessionBuilder.addSentPackets(workerBuilder.getSentPackets());
    }

    private void combineCollectedData(ExecutionContext executionContext) {
        getExecutionContext().getStoreCommand().addAll(executionContext.getStoreCommand().getChildren());
    }

    public GroupedDeviceCommand next() {
        return groupedDeviceCommands.poll();
    }

    /**
     * Each {@link ParallelWorkerScheduledJob} can call this method ones!
     * Calling this method indicates that there are no GroupedDeviceCommands anymore to process and the storage of the data can begin if it was the last one...
     *
     * @param workerScheduledJob the worker that finished its tasks and could not get a new one ...
     */
    public void complete(ParallelWorkerScheduledJob workerScheduledJob) {
        if (workerScheduledJob != null && workerScheduledJob.getExecutionContext() != null) { // the ConcurrentHashMap doesn't work well with NULL values ...
            completedWorkers.put(workerScheduledJob, workerScheduledJob.getExecutionContext());
            finish.countDown();
        }
    }

    public void completedASingleJob() {
        finish.countDown();
    }

    @Override
    public void unlock() {
        this.start.countDown(); // safety
        super.unlock();
    }

    @Override
    public void releaseToken() {
        this.start.countDown(); // safety
        super.releaseToken();
    }
}