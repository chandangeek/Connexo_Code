package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.configuration.rest.ConnectionStrategyAdapter;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
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
    private static final ConnectionTaskLifecycleStatusAdaptor CONNECTION_TASK_LIFECYCLE_STATUS_ADAPTOR = new ConnectionTaskLifecycleStatusAdaptor();
    private static final ConnectionStrategyAdapter CONNECTION_STRATEGY_ADAPTER = new ConnectionStrategyAdapter();

    private final Thesaurus thesaurus;
    private final DeviceDataService deviceDataService;
    private final Provider<ComTaskExecutionInfoFactory> comTaskExecutionInfoFactory;

    @Inject
    public ConnectionTaskInfoFactory(Thesaurus thesaurus, DeviceDataService deviceDataService, Provider<ComTaskExecutionInfoFactory> comTaskExecutionInfoFactoryProvider) {
        this.thesaurus = thesaurus;
        this.deviceDataService = deviceDataService;
        this.comTaskExecutionInfoFactory = comTaskExecutionInfoFactoryProvider;
    }

    public ConnectionTaskInfo from(ConnectionTask<?, ?> connectionTask, Optional<ComSession> lastComSessionOptional, List<ComTaskExecution> comTaskExecutions) throws Exception {
        ConnectionTaskInfo info = new ConnectionTaskInfo();
        info.device=new IdWithNameInfo(connectionTask.getDevice().getmRID(), connectionTask.getDevice().getName());
        info.deviceType=new IdWithNameInfo(connectionTask.getDevice().getDeviceType());
        info.deviceConfiguration=new IdWithNameInfo(connectionTask.getDevice().getDeviceConfiguration());
        info.currentState = null;// TODO wait for merge

        info.latestStatus=new LatestStatusInfo();
        info.latestStatus.id =connectionTask.getStatus();
        info.latestStatus.displayValue=thesaurus.getString(CONNECTION_TASK_LIFECYCLE_STATUS_ADAPTOR.marshal(connectionTask.getStatus()), null);

        if (lastComSessionOptional.isPresent()) {
            ComSession comSession = lastComSessionOptional.get();
            info.latestResult = new SuccessIndicatorInfo(comSession.getSuccessIndicator(), thesaurus);
            for (ComTaskExecution comTaskExecution : connectionTask.getDevice().getComTaskExecutions()) {
                if (comTaskExecution.getConnectionTask().getId()==connectionTask.getId()) {
                    info.latestResult.retries=comTaskExecution.getCurrentTryCount();
                }
            }

            info.taskCount = new ComTaskCountInfo();
            info.taskCount.numberOfSuccessfulTasks = comSession.getNumberOfSuccessFulTasks();
            info.taskCount.numberOfFailedTasks = comSession.getNumberOfFailedTasks();
            info.taskCount.numberOfIncompleteTasks = comSession.getNumberOfPlannedButNotExecutedTasks();

            info.startDateTime=comSession.getStartDate();
            info.endDateTime=comSession.getStopDate();
            info.duration=new TimeDurationInfo(new TimeDuration(comSession.getTotalDuration().toStandardSeconds().getSeconds()));
        }
        info.communicationTasks=new ComTaskListInfo();
        info.communicationTasks.count=comTaskExecutions.size();
        info.communicationTasks.communicationsTasks= comTaskExecutionInfoFactory.get().from(comTaskExecutions);

        info.comPortPool = new IdWithNameInfo(connectionTask.getComPortPool());
        info.direction=thesaurus.getString(connectionTask.getConnectionType().getDirection().name(),null);
        info.connectionType = ConnectionTypeRule.getConnectionTypeName(connectionTask.getConnectionType().getClass()).orNull();
        info.connectionMethod = new IdWithNameInfo();
        info.connectionMethod.id = connectionTask.getPartialConnectionTask().getId();
        info.connectionMethod.name = connectionTask.getPartialConnectionTask().getName();
        if (connectionTask.isDefault()) {
            info.connectionMethod.name+="( "+thesaurus.getString("default", "default")+" )";
        }
        ComServer executingComServer = connectionTask.getExecutingComServer();
        if (executingComServer!=null) {
            info.comServer = new IdWithNameInfo(executingComServer);
        }
        if (connectionTask instanceof ScheduledConnectionTask) {
            ScheduledConnectionTask scheduledConnectionTask = (ScheduledConnectionTask) connectionTask;
            info.connectionStrategy=new ConnectionStrategyInfo();
            info.connectionStrategy.id=scheduledConnectionTask.getConnectionStrategy();
            info.connectionStrategy.displayValue=thesaurus.getString(CONNECTION_STRATEGY_ADAPTER.marshal(scheduledConnectionTask.getConnectionStrategy()), scheduledConnectionTask.getConnectionStrategy().name());
            ComWindow communicationWindow = scheduledConnectionTask.getCommunicationWindow();
            info.window= communicationWindow.getStart()+" - "+communicationWindow.getEnd();
            info.nextExecution=scheduledConnectionTask.getPlannedNextExecutionTimestamp();
        }
        return info;
    }

}
