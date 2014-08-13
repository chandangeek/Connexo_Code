package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.configuration.rest.ConnectionStrategyAdapter;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.protocols.mdc.ConnectionTypeRule;
import com.google.common.base.Optional;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by bvn on 8/11/14.
 */
public class ConnectionTaskInfo {
    private static final ConnectionTaskLifecycleStatusAdaptor CONNECTION_TASK_LIFECYCLE_STATUS_ADAPTOR = new ConnectionTaskLifecycleStatusAdaptor();
    private static final ConnectionStrategyAdapter CONNECTION_STRATEGY_ADAPTER = new ConnectionStrategyAdapter();

    public IdWithNameInfo device;
    public IdWithNameInfo deviceConfiguration;
    public IdWithNameInfo deviceType;
    public TaskStatusInfo currentState; // task status
    public LatestStatusInfo latestStatus;
    public SuccessIndicatorInfo latestResult;
    public ComTaskCountInfo taskCount;
    public Date startDateTime;
    public Date endDateTime;
    public TimeDurationInfo duration;
    public IdWithNameInfo comPortPool;
    public String direction;
    public String connectionType;
    public IdWithNameInfo comServer;
    public IdWithNameInfo connectionMethod;
    public String window;
    public ConnectionStrategyInfo connectionStrategy;
    public Date nextExecution;
    public ComTaskListInfo communicationTasks;

    public static ConnectionTaskInfo from(ConnectionTask<?, ?> connectionTask, Optional<ComSession> lastComSessionOptional, List<ComTaskExecution> comTaskExecutions, Thesaurus thesaurus) throws Exception {
        ConnectionTaskInfo info = new ConnectionTaskInfo();
        info.device=new IdWithNameInfo(connectionTask.getDevice().getmRID(), connectionTask.getDevice().getName());
        info.deviceType=new IdWithNameInfo(connectionTask.getDevice().getDeviceType().getId(), connectionTask.getDevice().getDeviceType().getName());
        info.deviceConfiguration=new IdWithNameInfo(connectionTask.getDevice().getDeviceConfiguration().getId(), connectionTask.getDevice().getDeviceConfiguration().getName());
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
            info.duration=new TimeDurationInfo(new TimeDuration((int) comSession.getTotalTime()));
        }
        info.communicationTasks=new ComTaskListInfo();
        info.communicationTasks.count=comTaskExecutions.size();
        info.communicationTasks.communicationsTasks= ComTaskExecutionInfo.from(comTaskExecutions, thesaurus);

        info.comPortPool = new IdWithNameInfo(connectionTask.getComPortPool().getId(), connectionTask.getComPortPool().getName());
        info.direction=thesaurus.getString(connectionTask.getConnectionType().getDirection().name(),null);
        info.connectionType = ConnectionTypeRule.getConnectionTypeName(connectionTask.getConnectionType().getClass()).orNull();
        info.connectionMethod = new IdWithNameInfo();
        info.connectionMethod.name = connectionTask.getPartialConnectionTask().getName();
        if (connectionTask.isDefault()) {
            info.connectionMethod.name+="( "+thesaurus.getString("default", "default")+" )";
        }
        ComServer executingComServer = connectionTask.getExecutingComServer();
        if (executingComServer!=null) {
            info.comServer = new IdWithNameInfo(executingComServer.getId(), executingComServer.getName());
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

class LatestStatusInfo {
    @XmlJavaTypeAdapter(ConnectionTaskLifecycleStatusAdaptor.class)
    public ConnectionTask.ConnectionTaskLifecycleStatus id;
    public String displayValue;
}

class ConnectionStrategyInfo {
    @XmlJavaTypeAdapter(ConnectionStrategyAdapter.class)
    public ConnectionStrategy id;
    public String displayValue;
}

class ComTaskCountInfo {
    public long numberOfSuccessfulTasks;
    public long numberOfFailedTasks;
    public long numberOfIncompleteTasks;
}

class ComTaskListInfo {
    public int count;
    public List<ComTaskExecutionInfo> communicationsTasks;
}
