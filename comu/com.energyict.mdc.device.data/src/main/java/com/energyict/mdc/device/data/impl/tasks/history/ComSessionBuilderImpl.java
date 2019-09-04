/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.history;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.Counter;
import com.elster.jupiter.util.Counters;
import com.elster.jupiter.util.LongCounter;
import com.energyict.mdc.device.data.impl.tasks.ScheduledConnectionTaskImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.ComSessionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.tasks.ComTask;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@XmlRootElement
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public class ComSessionBuilderImpl implements ComSessionBuilder {
    public class UnderConstruction implements ComSessionBuilder {

        private LongCounter sentBytes = Counters.newStrictLongCounter();
        private LongCounter receivedBytes = Counters.newStrictLongCounter();
        private LongCounter sentPackets = Counters.newStrictLongCounter();
        private LongCounter receivedPackets = Counters.newStrictLongCounter();

        @XmlElement(type = ComSessionImpl.class)
        private ComSessionImpl comSession;

        private final Counter successfulTasks = Counters.newLenientNonNegativeCounter();
        private final Counter failedTasks = Counters.newStrictCounter();
        private final Counter notExecutedTasks = Counters.newStrictCounter();
        private final List<ComTaskExecutionSessionBuilderImpl> comTaskExecutions = new ArrayList<>();

        @Override
        public ComTaskExecutionSessionBuilder addComTaskExecutionSession(ComTaskExecution comTaskExecution, ComTask comTask, Instant startDate) {
            ComTaskExecutionSessionBuilderImpl builder = new ComTaskExecutionSessionBuilderImpl(parentBuilder(), comTaskExecution, comTask, startDate);
            comTaskExecutions.add(builder);
            return builder;
        }

        @Override
        public void addComTaskExecutionSession(ComTaskExecutionSessionBuilder comTaskExecutionSessionBuilder) {
            comTaskExecutions.add((ComTaskExecutionSessionBuilderImpl) comTaskExecutionSessionBuilder);
        }

        @Override
        public ConnectionTask getConnectionTask() {
            return this.comSession.getConnectionTask();
        }

        @Override
        @XmlElement(type = ScheduledConnectionTaskImpl.class)
        public void setConnectionTask(ConnectionTask connectionTask) {
            this.comSession.setConnectionTask(connectionTask);
        }

        public ComSessionBuilder addReceivedBytes(long numberOfBytes) {
            receivedBytes.add(numberOfBytes);
            return parentBuilder();
        }

        private ComSessionBuilderImpl parentBuilder() {
            return ComSessionBuilderImpl.this;
        }

        public ComSessionBuilder addReceivedPackets(long numberOfPackets) {
            receivedPackets.add(numberOfPackets);
            return parentBuilder();
        }

        public ComSessionBuilder addSentBytes(long numberOfBytes) {
            sentBytes.add(numberOfBytes);
            return parentBuilder();
        }

        public ComSessionBuilder addSentPackets(long numberOfPackets) {
            sentPackets.add(numberOfPackets);
            return parentBuilder();
        }

        @Override
        public ComSessionBuilder resetReceivedBytes() {
            receivedBytes = Counters.newStrictLongCounter();
            return this;
        }

        @Override
        public ComSessionBuilder resetReceivedPackets() {
            receivedPackets = Counters.newStrictLongCounter();
            return this;
        }

        @Override
        public ComSessionBuilder resetSentBytes() {
            sentBytes = Counters.newStrictLongCounter();
            return this;
        }

        @Override
        public ComSessionBuilder resetSentPackets() {
            sentPackets = Counters.newStrictLongCounter();
            return this;
        }

        @Override
        public long getSentBytes() {
            return sentBytes.getValue();
        }

        public void setSentBytes(long sentBytes) {
            this.sentBytes = Counters.newStrictLongCounter();
            this.sentBytes.add(sentBytes);
        }

        @Override
        public long getReceivedBytes() {
            return receivedBytes.getValue();
        }

        public void setReceivedBytes(long receivedBytes) {
            this.receivedBytes = Counters.newStrictLongCounter();
            this.receivedBytes.add(receivedBytes);
        }

        @Override
        public long getSentPackets() {
            return sentPackets.getValue();
        }

        public void setSentPackets(long sentPackets) {
            this.sentPackets = Counters.newStrictLongCounter();
            this.sentPackets.add(sentPackets);
        }

        @Override
        public long getReceivedPackets() {
            return receivedPackets.getValue();
        }

        public void setReceivedPackets(long receivedPackets) {
            this.receivedPackets = Counters.newStrictLongCounter();
            this.receivedPackets.add(receivedPackets);
        }

        @Override
        public ComSessionBuilder connectDuration(Duration duration) {
            comSession.setConnectDuration(duration);
            return parentBuilder();
        }

        @Override
        public EndedComSessionBuilder endSession(Instant stopTime, ComSession.SuccessIndicator successIndicator) {
            state = COMPLETE;
            comSession.setSuccessfulTasks(successfulTasks.getValue());
            comSession.setFailedTasks(failedTasks.getValue());
            comSession.setNotExecutedTasks(notExecutedTasks.getValue());
            comSession.setStopTime(stopTime);
            comSession.setSuccessIndicator(successIndicator);

            final ComSessionImpl completeComSession = comSession;
            completeComSession.setNumberOfBytesReceived(receivedBytes.getValue());
            completeComSession.setNumberOfBytesSent(sentBytes.getValue());
            completeComSession.setNumberOfPacketsReceived(receivedPackets.getValue());
            completeComSession.setNumberOfPacketsSent(sentPackets.getValue());
            for (ComSessionJournalEntry journalEntry : comSession.getJournalEntries()) {
                if (journalEntry.getComSession() == null)
                    journalEntry.setComSession(comSession);
            }
            for (ComTaskExecutionSessionBuilderImpl comTaskExecutionSessionBuilder : comTaskExecutions) {
                ComTaskExecutionSessionImpl comTaskExecutionSession = comTaskExecutionSessionBuilder.addTo(completeComSession);
                comTaskExecutionSession.setNumberOfBytesReceived(comTaskExecutionSessionBuilder.getReceivedBytes());
                comTaskExecutionSession.setNumberOfBytesSent(comTaskExecutionSessionBuilder.getSentBytes());
                comTaskExecutionSession.setNumberOfPacketsReceived(comTaskExecutionSessionBuilder.getReceivedPackets());
                comTaskExecutionSession.setNumberOfPacketsSent(comTaskExecutionSessionBuilder.getSentPackets());
            }

            return () -> {
                completeComSession.save();
                return completeComSession;
            };
        }

        @Override
        public ComSessionBuilder incrementSuccessFulTasks(int numberOfSuccessFulTasks) {
            successfulTasks.add(numberOfSuccessFulTasks);
            return parentBuilder();
        }

        @Override
        public ComSessionBuilder incrementFailedTasks(int numberOfFailedTasks) {
            failedTasks.add(numberOfFailedTasks);
            return parentBuilder();
        }

        @Override
        public ComSessionBuilder incrementNotExecutedTasks(int numberOfPlannedButNotExecutedTasks) {
            notExecutedTasks.add(numberOfPlannedButNotExecutedTasks);
            return parentBuilder();
        }

        @Override
        public ComSessionBuilder setSuccessFulTasks(int numberOfSuccessFulTasks) {
            this.successfulTasks.reset();
            return this.incrementSuccessFulTasks(numberOfSuccessFulTasks);
        }

        @Override
        public ComSessionBuilder setFailedTasks(int numberOfFailedTasks) {
            this.failedTasks.reset();
            return this.incrementFailedTasks(numberOfFailedTasks);
        }

        @Override
        public ComSessionBuilder setNotExecutedTasks(int numberOfPlannedButNotExecutedTasks) {
            this.notExecutedTasks.reset();
            return this.incrementNotExecutedTasks(numberOfPlannedButNotExecutedTasks);
        }

        @Override
        public ComSessionBuilder addJournalEntry(Instant timestamp, ComServer.LogLevel logLevel, String message) {
            comSession.createJournalEntry(timestamp, logLevel, message);
            return parentBuilder();
        }

        @Override
        public ComSessionBuilder addJournalEntry(Instant timestamp, ComServer.LogLevel logLevel, String message, Throwable cause) {
            comSession.createJournalEntry(timestamp, logLevel, message, cause);
            return parentBuilder();
        }

        @Override
        public ComSessionBuilder addJournalEntry(Instant timestamp, ComServer.LogLevel logLevel, Throwable cause) {
            comSession.createJournalEntry(timestamp, logLevel, this.extractMessageFrom(cause), cause);
            return parentBuilder();
        }

        @Override
        public void addJournalEntry(ComSessionJournalEntry entry) {
            comSession.addJournalEntry(entry);
        }

        @Override
        @XmlElement(type = ComSessionJournalEntryImpl.class)
        public List<ComSessionJournalEntry> getJournalEntries() {
            return comSession.getJournalEntries();
        }

        private String extractMessageFrom(Throwable cause) {
            if (Objects.nonNull(cause.getMessage())) {
                return cause.getMessage();
            }
            else {
                return cause.toString();
            }
        }

        @Override
        public ComSessionBuilder storeDuration(Duration duration) {
            comSession.setStoreDuration(duration);
            return parentBuilder();
        }

        @Override
        public ComSessionBuilder talkDuration(Duration duration) {
            comSession.setTalkDuration(duration);
            return parentBuilder();
        }

        @Override
        public Optional<ComTaskExecutionSessionBuilder> findFor(ComTaskExecution comTaskExecution) {
            for (ComTaskExecutionSessionBuilderImpl taskExecution : comTaskExecutions) {
                if (taskExecution.isFor(comTaskExecution)) {
                    return Optional.<ComTaskExecutionSessionBuilder>of(taskExecution);
                }
            }
            return Optional.empty();
        }

        @Override
        @XmlElement(type = ComTaskExecutionSessionBuilderImpl.class)
        public List<? extends ComTaskExecutionSessionBuilder> getComTaskExecutionSessionBuilders() {
            return comTaskExecutions;
        }

        public UnderConstruction(){
            comSession = new ComSessionImpl();
        }

        private UnderConstruction(DataModel dataModel, ConnectionTask<?, ?> connectionTask, ComPortPool comPortPool, ComPort comPort, Instant startTime) {
            comSession = ComSessionImpl.from(dataModel, connectionTask, comPortPool, comPort, startTime);
        }

        public void injectServices (DataModel dataModel, ConnectionTaskService connectionTaskService, Thesaurus thesaurus) {
            comSession.injectServices(dataModel, connectionTaskService, thesaurus);
        }
    }

    private static final ComSessionBuilder COMPLETE = (ComSessionBuilder) Proxy.newProxyInstance(ComSessionBuilderImpl.class.getClassLoader(), new Class<?>[] {ComSessionBuilder.class}, (proxy, method, args) -> {
        throw new IllegalStateException("endSession() has already been called on this builder.");
    });

    @XmlElement(type = UnderConstruction.class)
    private ComSessionBuilder state;

    public ComSessionBuilderImpl() {
        state = new UnderConstruction();
    }

    public ComSessionBuilderImpl(DataModel dataModel, ConnectionTask<?, ?> connectionTask, ComPortPool comPortPool, ComPort comPort, Instant startTime) {
        state = new UnderConstruction(dataModel, connectionTask, comPortPool, comPort, startTime);
    }

    public void injectServices (DataModel dataModel, ConnectionTaskService connectionTaskService, Thesaurus thesaurus) {
        state.injectServices(dataModel, connectionTaskService, thesaurus);
    }

    @Override
    @XmlElement(type = ScheduledConnectionTaskImpl.class)
    public ConnectionTask getConnectionTask() {
        return state.getConnectionTask();
    }

    @Override
    public void setConnectionTask(ConnectionTask connectionTask) {
        state.setConnectionTask(connectionTask);
    }

//    @Override
//    public ComPortPool getComPortPool() {
//        return state.getComPortPool();
//    }
//
//    @Override
//    public void setComPortPool(ComPortPool comPortPool) {
//        state.setComPortPool(comPortPool);
//    }
//
//    @Override
//    public ComPort getComPort() {
//        return state.getComPort();
//    }
//
//    @Override
//    public void setComPort(ComPort comPort) {
//        state.setComPort(comPort);
//    }
//
//    @Override
//    public Instant getStartTime() {
//        return state.getStartTime();
//    }
//
//    @Override
//    public void setStartTime(Instant startTime) {
//        state.setStartTime(startTime);
//    }

    @Override
    @XmlElement(type = ComTaskExecutionSessionBuilderImpl.class)
    public List<? extends ComTaskExecutionSessionBuilder> getComTaskExecutionSessionBuilders() {
        return state.getComTaskExecutionSessionBuilders();
    }

    @Override
    public ComTaskExecutionSessionBuilder addComTaskExecutionSession(ComTaskExecution comTaskExecution, ComTask comTask, Instant startDate) {
        return state.addComTaskExecutionSession(comTaskExecution, comTask, startDate);
    }

    @Override
    public void addComTaskExecutionSession(ComTaskExecutionSessionBuilder comTaskExecutionSessionBuilder) {
        state.addComTaskExecutionSession(comTaskExecutionSessionBuilder);
    }

    @Override
    public ComSessionBuilder addReceivedBytes(long numberOfBytes) {
        return state.addReceivedBytes(numberOfBytes);
    }

    @Override
    public ComSessionBuilder addReceivedPackets(long numberOfPackets) {
        return state.addReceivedPackets(numberOfPackets);
    }

    @Override
    public ComSessionBuilder addSentBytes(long numberOfBytes) {
        return state.addSentBytes(numberOfBytes);
    }

    @Override
    public ComSessionBuilder addSentPackets(long numberOfPackets) {
        return state.addSentPackets(numberOfPackets);
    }

    @Override
    public ComSessionBuilder resetReceivedBytes() {
        return state.resetReceivedBytes();
    }

    @Override
    public ComSessionBuilder resetReceivedPackets() {
        return state.resetReceivedPackets();
    }

    @Override
    public long getSentBytes() {
        return state.getSentBytes();
    }

    @Override
    public long getReceivedBytes() {
        return state.getReceivedBytes();
    }

    @Override
    public long getSentPackets() {
        return state.getSentPackets();
    }

    @Override
    public long getReceivedPackets() {
        return state.getReceivedPackets();
    }

    @Override
    public void setSentBytes(long sentBytes) {
        state.setSentBytes(sentBytes);
    }

    @Override
    public void setReceivedBytes(long receivedBytes) {
        state.setReceivedBytes(receivedBytes);
    }

    @Override
    public void setSentPackets(long sentPackets) {
        state.setSentPackets(sentPackets);
    }

    @Override
    public void setReceivedPackets(long receivedPackets) {
        state.setReceivedPackets(receivedPackets);
    }

    @Override
    public ComSessionBuilder resetSentBytes() {
        return state.resetSentBytes();
    }

    @Override
    public ComSessionBuilder resetSentPackets() {
        return state.resetSentPackets();
    }

    @Override
    public ComSessionBuilder connectDuration(Duration duration) {
        return state.connectDuration(duration);
    }

    @Override
    public EndedComSessionBuilder endSession(Instant stopTime, ComSession.SuccessIndicator successIndicator) {
        return state.endSession(stopTime, successIndicator);
    }

    @Override
    public ComSessionBuilder incrementFailedTasks(int numberOfFailedTasks) {
        return state.incrementFailedTasks(numberOfFailedTasks);
    }

    @Override
    public ComSessionBuilder incrementNotExecutedTasks(int numberOfPlannedButNotExecutedTasks) {
        return state.incrementNotExecutedTasks(numberOfPlannedButNotExecutedTasks);
    }

    @Override
    public ComSessionBuilder incrementSuccessFulTasks(int numberOfSuccessFulTasks) {
        return state.incrementSuccessFulTasks(numberOfSuccessFulTasks);
    }

    public ComSessionBuilder setFailedTasks(int numberOfFailedTasks) {
        return state.setFailedTasks(numberOfFailedTasks);
    }

    @Override
    public ComSessionBuilder setNotExecutedTasks(int numberOfPlannedButNotExecutedTasks) {
        return state.setNotExecutedTasks(numberOfPlannedButNotExecutedTasks);
    }

    @Override
    public ComSessionBuilder setSuccessFulTasks(int numberOfSuccessFulTasks) {
        return state.setSuccessFulTasks(numberOfSuccessFulTasks);
    }

    @Override
    public ComSessionBuilder storeDuration(Duration duration) {
        return state.storeDuration(duration);
    }

    @Override
    public ComSessionBuilder talkDuration(Duration duration) {
        return state.talkDuration(duration);
    }

    @Override
    public ComSessionBuilder addJournalEntry(Instant timestamp, ComServer.LogLevel logLevel, String message) {
        return state.addJournalEntry(timestamp, logLevel, message);
    }

    @Override
    public ComSessionBuilder addJournalEntry(Instant timestamp, ComServer.LogLevel logLevel, String message, Throwable cause) {
        return state.addJournalEntry(timestamp, logLevel, message, cause);
    }

    @Override
    public ComSessionBuilder addJournalEntry(Instant timestamp, ComServer.LogLevel logLevel, Throwable cause) {
        return state.addJournalEntry(timestamp, logLevel, cause);
    }

    @Override
    public Optional<ComTaskExecutionSessionBuilder> findFor(ComTaskExecution comTaskExecution) {
        return state.findFor(comTaskExecution);
    }

    @Override
    public void addJournalEntry(ComSessionJournalEntry entry) {
        state.addJournalEntry(entry);
    }

    @Override
    @XmlElement(type = ComSessionJournalEntryImpl.class)
    public List<ComSessionJournalEntry> getJournalEntries() {
        return state.getJournalEntries();
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }
}