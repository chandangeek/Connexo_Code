package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.configuration.rest.ConnectionStrategyAdapter;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by bvn on 8/11/14.
 */
public class ConnectionTaskInfo {

    public IdWithNameInfo device;
    public IdWithNameInfo deviceConfiguration;
    public IdWithNameInfo deviceType;
    public TaskStatusInfo currentState;
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
