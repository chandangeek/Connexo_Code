package com.energyict.mdc.device.data.rest;

import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.configuration.rest.ConnectionStrategyAdapter;
import com.energyict.mdc.device.data.rest.DeviceConnectionTaskInfo.ComTaskCountInfo;
import com.energyict.mdc.device.data.rest.DeviceConnectionTaskInfo.ConnectionMethodInfo;
import com.energyict.mdc.device.data.rest.DeviceConnectionTaskInfo.ConnectionStrategyInfo;
import com.energyict.mdc.device.data.rest.DeviceConnectionTaskInfo.LatestStatusInfo;
import com.energyict.mdc.device.data.rest.impl.MessageSeeds;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;

import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.time.Duration;
import java.time.temporal.ChronoField;
import java.util.Optional;
import java.util.function.Supplier;

public class DeviceConnectionTaskInfoFactory {
    private static final ConnectionTaskSuccessIndicatorAdapter SUCCESS_INDICATOR_ADAPTER = new ConnectionTaskSuccessIndicatorAdapter();
    private static final ConnectionStrategyAdapter CONNECTION_STRATEGY_ADAPTER = new ConnectionStrategyAdapter();

    private final Thesaurus thesaurus;

    @Inject
    public DeviceConnectionTaskInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public DeviceConnectionTaskInfo from(ConnectionTask<?, ?> connectionTask, Optional<ComSession> lastComSessionOptional) {
        return this.from(connectionTask, lastComSessionOptional, DeviceConnectionTaskInfo::new);
    }

    protected <T extends DeviceConnectionTaskInfo> T from(ConnectionTask<?, ?> connectionTask, Optional<ComSession> lastComSessionOptional, Supplier<T> supplier) {
        T info = supplier.get();
        info.id=connectionTask.getId();
        info.latestStatus=new LatestStatusInfo();
        info.latestStatus.id =connectionTask.getSuccessIndicator();
        info.latestStatus.displayValue=thesaurus.getString(SUCCESS_INDICATOR_ADAPTER.marshal(connectionTask.getSuccessIndicator()), SUCCESS_INDICATOR_ADAPTER.marshal(connectionTask.getSuccessIndicator()));
        if (lastComSessionOptional.isPresent()) {
            ComSession comSession = lastComSessionOptional.get();
            info.latestResult = new SuccessIndicatorInfo(comSession.getSuccessIndicator(), thesaurus);
            if (connectionTask instanceof OutboundConnectionTask<?>) {
                info.latestResult.retries=((OutboundConnectionTask<?>)connectionTask).getCurrentTryCount();
            }
            info.taskCount = new ComTaskCountInfo();
            info.taskCount.numberOfSuccessfulTasks = comSession.getNumberOfSuccessFulTasks();
            info.taskCount.numberOfFailedTasks = comSession.getNumberOfFailedTasks();
            info.taskCount.numberOfIncompleteTasks = comSession.getNumberOfPlannedButNotExecutedTasks();
            info.startDateTime = comSession.getStartDate().with(ChronoField.MILLI_OF_SECOND, 0);
            info.endDateTime = comSession.getStopDate().with(ChronoField.MILLI_OF_SECOND, 0);
            info.duration=new TimeDurationInfo(Duration.ofMillis(info.endDateTime.toEpochMilli() - info.startDateTime.toEpochMilli()).getSeconds());   // JP-6022
            info.comPort = new IdWithNameInfo(comSession.getComPort());
            info.comServer = new IdWithNameInfo(comSession.getComPort().getComServer());
            info.comSessionId = comSession.getId();
        }
        info.comPortPool = new IdWithNameInfo(connectionTask.getComPortPool());
        info.direction=thesaurus.getString(connectionTask.getConnectionType().getDirection().name(),connectionTask.getConnectionType().getDirection().name());
        info.connectionType = connectionTask.getPluggableClass().getName();
        info.connectionMethod = new ConnectionMethodInfo();
        info.connectionMethod.id = connectionTask.getPartialConnectionTask().getId();
        info.connectionMethod.name = connectionTask.getPartialConnectionTask().getName();
        info.connectionMethod.status = connectionTask.getStatus();
        info.connectionMethod.isDefault = connectionTask.isDefault();
        if (connectionTask instanceof ScheduledConnectionTask) {
            ScheduledConnectionTask scheduledConnectionTask = (ScheduledConnectionTask) connectionTask;
            if (scheduledConnectionTask.getTaskStatus()!=null) {
                info.currentState = new TaskStatusInfo(scheduledConnectionTask.getTaskStatus(), thesaurus);
            }
            info.connectionStrategy=new ConnectionStrategyInfo();
            info.connectionStrategy.id=scheduledConnectionTask.getConnectionStrategy();
            info.connectionStrategy.displayValue=thesaurus.getString(CONNECTION_STRATEGY_ADAPTER.marshal(scheduledConnectionTask.getConnectionStrategy()), scheduledConnectionTask.getConnectionStrategy().name());
            ComWindow communicationWindow = scheduledConnectionTask.getCommunicationWindow();
            if (communicationWindow!=null &&
                    (communicationWindow.getStart().getMillis()!=0 || communicationWindow.getEnd().getMillis()!=0)) {
                info.window = communicationWindow.getStart() + " - " + communicationWindow.getEnd();
            } else {
                info.window = thesaurus.getString(MessageSeeds.NO_RESTRICTIONS.getKey(), MessageSeeds.NO_RESTRICTIONS.getDefaultFormat());
            }
            info.nextExecution=scheduledConnectionTask.getNextExecutionTimestamp();
        }
        return info;
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }
}
