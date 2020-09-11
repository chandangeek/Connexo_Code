/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.history.ComSessionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    CommandRoot workerStarted(long threadId){
        if (commandRoot == null){
            commandRoot = initCommandRoot();
        }
        return ((ParallelCommandRoot) commandRoot).workerStarted(threadId);
    }

    protected CommandRoot initCommandRoot(){
        return new ParallelCommandRoot(getExecutionContext(), new ComCommandServiceProvider());
    }

    @Override
    public void execute() {
        Thread.currentThread().setName("ComPort ParallelRoot ScheduledJob for " + getComPort().getName() + "/"+Thread.currentThread().getId());
        try {
            boolean connectionEstablished;
            this.createExecutionContext();
            commandRoot = prepareAll(getComTaskExecutions());

            if (!commandRoot.hasGeneralSetupErrorOccurred() && hasCommandsToExecute()) {
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

    private boolean hasCommandsToExecute() {
        for (GroupedDeviceCommand groupedDeviceCommand : ((ParallelCommandRoot) commandRoot).groupedDeviceCommands) {
            if (groupedDeviceCommand.getCommands().size() > 0) {
                return true;
            }
        }
        return false;
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
        return this.next(getCommandRootForCurrentThread());
    }

    public GroupedDeviceCommand next(long threadId) {
        return this.next(((ParallelCommandRoot) this.commandRoot).getCommandRoot(threadId));
    }

    public GroupedDeviceCommand next(CommandRoot commandRoot) {
        GroupedDeviceCommand next =  groupedDeviceCommands.poll();
        if (next != null && commandRoot != null) {
            next.setCommandRoot(commandRoot);
        }
        return next;
    }

    private CommandRoot getCommandRootForCurrentThread(){
        return ((ParallelCommandRoot) this.commandRoot).getCommandRoot(Thread.currentThread().getId());
    }

    /**
     * Each {@link ParallelWorkerScheduledJob} can call this method ones!
     * Calling this method indicates that there are no GroupedDeviceCommands anymore to process and the storage of the data can begin if it was the last one...
     *
     * @param workerScheduledJob the worker that finished its tasks and could not get a new one ...
     */
    public void complete(ParallelWorkerScheduledJob workerScheduledJob) {
        if (workerScheduledJob != null && workerScheduledJob.getExecutionContext() != null) { // the ConcurrentHashMap doesn't work well with NULL values ...
            ((ParallelCommandRoot) this.commandRoot).workerEnded(workerScheduledJob.getThreadId());
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

    // Composite command root holding the individual command root for each parallel scheduled job
    static class ParallelCommandRoot implements CommandRoot {
        private Set<GroupedDeviceCommand> groupedDeviceCommands = new HashSet<>();
        private Map<Long, CommandRootImpl> parallelCommandRoots = new HashMap<>();
        private List<? extends ComTaskExecution> scheduledButNotPreparedComTaskExecutions = new ArrayList<>();
        private Throwable generalSetupError;
        private final ExecutionContext executionContext;
        private final CommandRoot.ServiceProvider serviceProvider;

        private ParallelCommandRoot(ExecutionContext executionContext, CommandRoot.ServiceProvider serviceProvider){
           this.executionContext = executionContext;
           this.serviceProvider = serviceProvider;
        }

        private GroupedDeviceCommand addGroupedDeviceCommand(OfflineDevice offlineDevice, DeviceProtocol deviceProtocol, DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet){
            GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(this, offlineDevice, deviceProtocol, deviceProtocolSecurityPropertySet);
            groupedDeviceCommands.add(groupedDeviceCommand);
            return groupedDeviceCommand;
        }

        CommandRoot workerStarted(long threadId){
            return parallelCommandRoots.put(threadId, new CommandRootImpl(executionContext, serviceProvider));
        }

        CommandRoot workerEnded(long threadId){
            return parallelCommandRoots.remove(threadId);
        }

        @Override
        public ExecutionContext getExecutionContext() {
            return executionContext;
        }

        @Override
        public boolean isExposeStoringException() {
            return false;
        }

        @Override
        public GroupedDeviceCommand getOrCreateGroupedDeviceCommand(OfflineDevice offlineDevice, DeviceProtocol deviceProtocol, DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
            return groupedDeviceCommands.stream().filter(dgc -> (dgc.getOfflineDevice().getId() == offlineDevice.getId() && dgc.hasSecurityPropertySet(deviceProtocolSecurityPropertySet))).findFirst()
                    .orElse(addGroupedDeviceCommand(offlineDevice, deviceProtocol, deviceProtocolSecurityPropertySet));
        }

        public CommandRoot getCommandRoot(long threadId){
             return parallelCommandRoots.get(threadId);
        }

        @Override
        public void removeAllGroupedDeviceCommands() {
            parallelCommandRoots.clear();
        }

        @Override
        public Map<ComCommandType, ComCommand> getCommands() {
            Map<ComCommandType, ComCommand> allCommands = new LinkedHashMap<>();
            parallelCommandRoots.values().stream().forEach(cr -> allCommands.putAll(cr.getCommands()));
            return allCommands;
        }

        @Override
        public void execute(boolean connectionEstablished) {
            parallelCommandRoots.values().stream().forEach((cr) -> cr.execute(connectionEstablished));
        }

        @Override
        public void connectionErrorOccurred() {
            parallelCommandRoots.values().stream().forEach(CommandRoot::connectionErrorOccurred);
        }

        @Override
        public boolean hasConnectionErrorOccurred() {
            return parallelCommandRoots.values().stream().filter(CommandRoot::hasConnectionErrorOccurred).findFirst().isPresent();
        }

        @Override
        public boolean hasConnectionSetupError() {
            return parallelCommandRoots.values().stream().filter(CommandRoot::hasConnectionSetupError).findFirst().isPresent();
        }


        @Override
        public void generalSetupErrorOccurred(Throwable e, List<? extends ComTaskExecution> comTaskExecutions) {
            generalSetupError = e;
            scheduledButNotPreparedComTaskExecutions = comTaskExecutions;
        }

        @Override
        public boolean hasGeneralSetupErrorOccurred() {
            return generalSetupError != null;
        }

        @Override
        public void connectionInterrupted() {
            parallelCommandRoots.values().stream().forEach(CommandRoot::connectionInterrupted);
        }

        @Override
        public boolean hasConnectionBeenInterrupted() {
            return parallelCommandRoots.values().stream().filter(CommandRoot::hasConnectionBeenInterrupted).findFirst().isPresent();
        }

        @Override
        public void connectionExecuted(boolean executed) {
        }

        @Override
        public boolean hasConnectionNotExecuted() {
            return parallelCommandRoots.values().stream().allMatch(CommandRoot::hasConnectionNotExecuted);
        }

        @Override
        public List<? extends ComTaskExecution> getScheduledButNotPreparedComTaskExecutions() {
            return scheduledButNotPreparedComTaskExecutions;
        }

        @Override
        public ServiceProvider getServiceProvider() {
            return serviceProvider;
        }

        @Override
        public Iterator<GroupedDeviceCommand> iterator() {
            return groupedDeviceCommands.iterator();
        }
    }
}