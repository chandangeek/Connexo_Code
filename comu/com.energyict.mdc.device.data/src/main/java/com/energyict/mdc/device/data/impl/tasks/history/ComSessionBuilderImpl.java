package com.energyict.mdc.device.data.impl.tasks.history;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.tasks.ComTask;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.Counter;
import com.elster.jupiter.util.Counters;
import com.elster.jupiter.util.LongCounter;

import java.lang.reflect.Proxy;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 28/04/2014
 * Time: 17:19
 */

public class ComSessionBuilderImpl implements ComSessionBuilder {
    private class UnderConstruction implements ComSessionBuilder {

        private final LongCounter sentBytes = Counters.newStrictLongCounter();
        private final LongCounter receivedBytes = Counters.newStrictLongCounter();
        private final LongCounter sentPackets = Counters.newStrictLongCounter();
        private final LongCounter receivedPackets = Counters.newStrictLongCounter();

        private ComSessionImpl comSession;

        private final Counter successfulTasks = Counters.newStrictCounter();
        private final Counter failedTasks = Counters.newStrictCounter();
        private final Counter notExecutedTasks = Counters.newLenientNonNegativeCounter();
        private final List<ComTaskExecutionSessionBuilderImpl> comTaskExecutions = new ArrayList<>();

        @Override
        public ComTaskExecutionSessionBuilder addComTaskExecutionSession(ComTaskExecution comTaskExecution, ComTask comTask, Device device, Instant startDate) {
            ComTaskExecutionSessionBuilderImpl builder = new ComTaskExecutionSessionBuilderImpl(parentBuilder(), comTaskExecution, comTask, device, startDate);
            comTaskExecutions.add(builder);
            return builder;
        }

        @Override
        public ConnectionTask getConnectionTask() {
            return this.comSession.getConnectionTask();
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

        private void decrementNotExecutedTasks(int nrOfTasks){
            notExecutedTasks.add(-nrOfTasks);
        }

        @Override
        public ComSessionBuilder incrementFailedTasks(int numberOfFailedTasks) {
            failedTasks.add(numberOfFailedTasks);
            decrementNotExecutedTasks(numberOfFailedTasks);
            return parentBuilder();
        }

        @Override
        public ComSessionBuilder incrementNotExecutedTasks(int numberOfPlannedButNotExecutedTasks) {
            notExecutedTasks.add(numberOfPlannedButNotExecutedTasks);
            return parentBuilder();
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

        private String extractMessageFrom(Throwable cause) {
            if (Objects.nonNull(cause.getMessage())) {
                return cause.getMessage();
            }
            else {
                return cause.toString();
            }
        }

        @Override
        public ComSessionBuilder incrementSuccessFulTasks(int numberOfSuccessFulTasks) {
            successfulTasks.add(numberOfSuccessFulTasks);
            decrementNotExecutedTasks(numberOfSuccessFulTasks);
            return parentBuilder();
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

        private UnderConstruction(DataModel dataModel, ConnectionTask<?, ?> connectionTask, ComPortPool comPortPool, ComPort comPort, Instant startTime) {
            comSession = ComSessionImpl.from(dataModel, connectionTask, comPortPool, comPort, startTime);
        }
    }

    private static final ComSessionBuilder COMPLETE = (ComSessionBuilder) Proxy.newProxyInstance(ComSessionBuilderImpl.class.getClassLoader(), new Class<?>[] {ComSessionBuilder.class}, (proxy, method, args) -> {
        throw new IllegalStateException("endSession() has already been called on this builder.");
    });

    private ComSessionBuilder state;

    public ComSessionBuilderImpl(DataModel dataModel, ConnectionTask<?, ?> connectionTask, ComPortPool comPortPool, ComPort comPort, Instant startTime) {
        state = new UnderConstruction(dataModel, connectionTask, comPortPool, comPort, startTime);
    }

    @Override
    public ConnectionTask getConnectionTask() {
        return state.getConnectionTask();
    }

    @Override
    public ComTaskExecutionSessionBuilder addComTaskExecutionSession(ComTaskExecution comTaskExecution, ComTask comTask, Device device, Instant startDate) {
        return state.addComTaskExecutionSession(comTaskExecution, comTask, device, startDate);
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

}