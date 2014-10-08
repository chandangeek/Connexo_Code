package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.configuration.rest.ConnectionStrategyAdapter;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.protocols.mdc.ConnectionTypeRule;
import com.google.common.base.Optional;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by bvn on 9/1/14.
 */
public class ConnectionTaskInfoFactory {
    private static final ConnectionTaskSuccessIndicatorAdapter SUCCESS_INDICATOR_ADAPTER = new ConnectionTaskSuccessIndicatorAdapter();
    private static final ConnectionStrategyAdapter CONNECTION_STRATEGY_ADAPTER = new ConnectionStrategyAdapter();

    private final Thesaurus thesaurus;
    private final Provider<ComTaskExecutionInfoFactory> comTaskExecutionInfoFactory;

    @Inject
    public ConnectionTaskInfoFactory(Thesaurus thesaurus, Provider<ComTaskExecutionInfoFactory> comTaskExecutionInfoFactoryProvider) {
        this.thesaurus = thesaurus;
        this.comTaskExecutionInfoFactory = comTaskExecutionInfoFactoryProvider;
    }

    public ConnectionTaskInfo from(ConnectionTask<?, ?> connectionTask, Optional<ComSession> lastComSessionOptional, List<ComTaskExecution> comTaskExecutions) throws Exception {
        ConnectionTaskInfo info = new ConnectionTaskInfo();
        Device device = connectionTask.getDevice();
        info.device=new IdWithNameInfo(device.getmRID(), device.getName());
        info.deviceType=new IdWithNameInfo(device.getDeviceType());
        info.deviceConfiguration=new IdWithNameInfo(device.getDeviceConfiguration());
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

            info.startDateTime=comSession.getStartDate();
            info.endDateTime=comSession.getStopDate();
            info.duration=new TimeDurationInfo(new TimeDuration((int)comSession.getTotalDuration().getMillis(),TimeDuration.MILLISECONDS));
        }
        info.communicationTasks=new ComTaskListInfo();
        info.communicationTasks.count=comTaskExecutions.size();
        info.communicationTasks.communicationsTasks= comTaskExecutionInfoFactory.get().from(comTaskExecutions);

        info.comPort = connectionTask.getLastComSession().isPresent()?new IdWithNameInfo(connectionTask.getLastComSession().get().getComPort()):null;
        info.direction=thesaurus.getString(connectionTask.getConnectionType().getDirection().name(),connectionTask.getConnectionType().getDirection().name());
        info.connectionType = ConnectionTypeRule.getConnectionTypeName(connectionTask.getConnectionType().getClass()).orNull();
        info.connectionMethod = new IdWithNameInfo();
        info.connectionMethod.id = connectionTask.getPartialConnectionTask().getId();
        info.connectionMethod.name = connectionTask.getPartialConnectionTask().getName();
        if (connectionTask.isDefault()) {
            info.connectionMethod.name+=" ("+thesaurus.getString(MessageSeeds.DEFAULT.getKey(), "default")+")";
        }
        ComServer executingComServer = connectionTask.getExecutingComServer();
        if (executingComServer!=null) {
            info.comServer = new IdWithNameInfo(executingComServer);
        }
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
            info.nextExecution=scheduledConnectionTask.getPlannedNextExecutionTimestamp();
        }
        return info;
    }

}
