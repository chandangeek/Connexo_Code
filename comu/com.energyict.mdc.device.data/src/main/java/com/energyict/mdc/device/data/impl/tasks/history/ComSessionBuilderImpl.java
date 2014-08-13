package com.energyict.mdc.device.data.impl.tasks.history;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.Counter;
import com.elster.jupiter.util.Counters;
import com.elster.jupiter.util.LongCounter;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.ComStatistics;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSessionBuilder;
import com.google.common.base.Optional;
import net.jcip.annotations.NotThreadSafe;
import org.joda.time.Duration;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyrights EnergyICT
 * Date: 28/04/2014
 * Time: 17:19
 */
@NotThreadSafe
public class ComSessionBuilderImpl implements ComSessionBuilder {
    private class UnderConstruction implements ComSessionBuilder {

        private final LongCounter sentBytes = Counters.newStrictLongCounter();
        private final LongCounter receivedBytes = Counters.newStrictLongCounter();
        private final LongCounter sentPackets = Counters.newStrictLongCounter();
        private final LongCounter receivedPackets = Counters.newStrictLongCounter();

        private final DataModel dataModel;

        private ComSessionImpl comSession;

        private final Counter successfulTasks = Counters.newStrictCounter();
        private final Counter failedTasks = Counters.newStrictCounter();
        private final Counter notExecutedTasks = Counters.newStrictCounter();
        private final List<ComTaskExecutionSessionBuilderImpl> comTaskExecutions = new ArrayList<>();

        @Override
        public ComTaskExecutionSessionBuilder addComTaskExecutionSession(ComTaskExecution comTaskExecution, Device device, Date startDate) {
            ComTaskExecutionSessionBuilderImpl builder = new ComTaskExecutionSessionBuilderImpl(parentBuilder(), comTaskExecution, device, startDate);
            comTaskExecutions.add(builder);
            return builder;
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
        public EndedComSessionBuilder endSession(Date stopTime, ComSession.SuccessIndicator successIndicator) {
            state = COMPLETE;
            comSession.setSuccessfulTasks(successfulTasks.getValue());
            comSession.setFailedTasks(failedTasks.getValue());
            comSession.setNotExecutedTasks(notExecutedTasks.getValue());
            comSession.setStopTime(stopTime);
            comSession.setSuccessIndicator(successIndicator);

            final ComSessionImpl completeComSession = comSession;
            final ComStatisticsImpl comStatistics = dataModel.getInstance(ComStatisticsImpl.class);
            comStatistics.setNumberOfBytesReceived(receivedBytes.getValue());
            comStatistics.setNrOfBytesSent(sentBytes.getValue());
            comStatistics.setNrOfPacketsReceived(receivedPackets.getValue());
            comStatistics.setNrOfPacketsSent(sentPackets.getValue());
            final Map<ComTaskExecutionSession, ComStatistics> statisticsMap = new HashMap<>();
            for (ComTaskExecutionSessionBuilderImpl comTaskExecutionSessionBuilder : comTaskExecutions) {
                ComTaskExecutionSession comTaskExecutionSession = comTaskExecutionSessionBuilder.addTo(completeComSession);
                ComStatisticsImpl stats = dataModel.getInstance(ComStatisticsImpl.class);
                stats.setNumberOfBytesReceived(comTaskExecutionSessionBuilder.getReceivedBytes());
                stats.setNrOfBytesSent(comTaskExecutionSessionBuilder.getSentBytes());
                stats.setNrOfPacketsReceived(comTaskExecutionSessionBuilder.getReceivedPackets());
                stats.setNrOfPacketsSent(comTaskExecutionSessionBuilder.getSentPackets());
                statisticsMap.put(comTaskExecutionSession, stats);
            }

            return new EndedComSessionBuilder() {
                @Override
                public ComSession create() {
                    DataMapper<ComStatistics> statisticsDataMapper = dataModel.mapper(ComStatistics.class);
                    for (Map.Entry<ComTaskExecutionSession, ComStatistics> entry : statisticsMap.entrySet()) {
                        ComStatistics stats = entry.getValue();
                        statisticsDataMapper.persist(stats);
                        entry.getKey().setStatistics(stats);
                    }
                    statisticsDataMapper.persist(comStatistics);
                    completeComSession.setStatistics(comStatistics);
                    completeComSession.save();
                    return completeComSession;
                }
            };
        }

        @Override
        public ComSessionBuilder incrementFailedTasks() {
            failedTasks.increment();
            return parentBuilder();
        }

        @Override
        public ComSessionBuilder incrementFailedTasks(int numberOfFailedTasks) {
            failedTasks.add(numberOfFailedTasks);
            return parentBuilder();
        }

        @Override
        public ComSessionBuilder incrementNotExecutedTasks() {
            notExecutedTasks.increment();
            return parentBuilder();
        }

        @Override
        public ComSessionBuilder incrementNotExecutedTasks(int numberOfPlannedButNotExecutedTasks) {
            notExecutedTasks.add(numberOfPlannedButNotExecutedTasks);
            return parentBuilder();
        }

        @Override
        public ComSessionBuilder addJournalEntry(Date timestamp, String message, Throwable cause) {
            comSession.createJournalEntry(timestamp, message, cause);
            return parentBuilder();
        }

        @Override
        public ComSessionBuilder incrementSuccessFulTasks() {
            successfulTasks.increment();
            return parentBuilder();
        }

        @Override
        public ComSessionBuilder incrementSuccessFulTasks(int numberOfSuccessFulTasks) {
            successfulTasks.add(numberOfSuccessFulTasks);
            return parentBuilder();
        }

        @Override
        public ComSessionBuilder storeDuration(Duration duration) {
            comSession.setStoreDuration(duration);
            return parentBuilder();
        }

        @Override
        public ComSessionBuilder successful() {
            comSession.setSuccessful(true);
            return parentBuilder();
        }

        @Override
        public ComSessionBuilder talkDuration(Duration duration) {
            comSession.setTalkDuration(duration);
            return parentBuilder();
        }

        @Override
        public ComSessionBuilder totalTime(long totalTime) {
            comSession.setTotalTime(totalTime);
            return parentBuilder();
        }

        @Override
        public ComSessionBuilder unsuccessful() {
            comSession.setSuccessful(false);
            return parentBuilder();
        }

        @Override
        public Optional<ComTaskExecutionSessionBuilder> findFor(ComTaskExecution comTaskExecution) {
            for (ComTaskExecutionSessionBuilderImpl taskExecution : comTaskExecutions) {
                if (taskExecution.isFor(comTaskExecution)) {
                    return Optional.<ComTaskExecutionSessionBuilder>of(taskExecution);
                }
            }
            return Optional.absent();
        }

        private UnderConstruction(DataModel dataModel, ConnectionTask<?, ?> connectionTask, ComPortPool comPortPool, ComPort comPort, Date startTime) {
            this.dataModel = dataModel;
            comSession = ComSessionImpl.from(dataModel, connectionTask, comPortPool, comPort, startTime);
        }
    }

    private static final ComSessionBuilder COMPLETE = (ComSessionBuilder) Proxy.newProxyInstance(ComSessionBuilderImpl.class.getClassLoader(), new Class<?>[] {ComSessionBuilder.class}, new InvocationHandler() {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            throw new IllegalStateException("endSession() has already been called on this builder.");
        }
    });

    private ComSessionBuilder state;

    public ComSessionBuilderImpl(DataModel dataModel, ConnectionTask<?, ?> connectionTask, ComPortPool comPortPool, ComPort comPort, Date startTime) {
        state = new UnderConstruction(dataModel, connectionTask, comPortPool, comPort, startTime);
    }

    @Override
    public ComTaskExecutionSessionBuilder addComTaskExecutionSession(ComTaskExecution comTaskExecution, Device device, Date startDate) {
        return state.addComTaskExecutionSession(comTaskExecution, device, startDate);
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
    public EndedComSessionBuilder endSession(Date stopTime, ComSession.SuccessIndicator successIndicator) {
        return state.endSession(stopTime, successIndicator);
    }

    @Override
    public ComSessionBuilder incrementFailedTasks() {
        return state.incrementFailedTasks();
    }

    @Override
    public ComSessionBuilder incrementFailedTasks(int numberOfFailedTasks) {
        return state.incrementFailedTasks(numberOfFailedTasks);
    }

    @Override
    public ComSessionBuilder incrementNotExecutedTasks() {
        return state.incrementNotExecutedTasks();
    }

    @Override
    public ComSessionBuilder incrementNotExecutedTasks(int numberOfPlannedButNotExecutedTasks) {
        return state.incrementNotExecutedTasks(numberOfPlannedButNotExecutedTasks);
    }

    @Override
    public ComSessionBuilder incrementSuccessFulTasks() {
        return state.incrementSuccessFulTasks();
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
    public ComSessionBuilder successful() {
        return state.successful();
    }

    @Override
    public ComSessionBuilder talkDuration(Duration duration) {
        return state.talkDuration(duration);
    }

    @Override
    public ComSessionBuilder totalTime(long totalTime) {
        return state.totalTime(totalTime);
    }

    @Override
    public ComSessionBuilder unsuccessful() {
        return state.unsuccessful();
    }

    @Override
    public ComSessionBuilder addJournalEntry(Date timestamp, String message, Throwable cause) {
        return state.addJournalEntry(timestamp, message, cause);
    }

    @Override
    public Optional<ComTaskExecutionSessionBuilder> findFor(ComTaskExecution comTaskExecution) {
        return state.findFor(comTaskExecution);
    }
}
