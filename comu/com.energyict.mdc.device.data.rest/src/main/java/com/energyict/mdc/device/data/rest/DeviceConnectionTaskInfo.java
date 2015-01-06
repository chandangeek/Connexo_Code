package com.energyict.mdc.device.data.rest;

import java.time.Instant;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.configuration.rest.ConnectionStrategyAdapter;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask.ConnectionTaskLifecycleStatus;

public class DeviceConnectionTaskInfo {
    public long id;
    public TaskStatusInfo currentState;
    public LatestStatusInfo latestStatus;
    public SuccessIndicatorInfo latestResult;
    public ComTaskCountInfo taskCount;
    public Instant startDateTime;
    public Instant endDateTime;
    public TimeDurationInfo duration;
    public IdWithNameInfo comPort;
    public IdWithNameInfo comPortPool;
    public String direction;
    public String connectionType;
    public IdWithNameInfo comServer;
    public ConnectionMethodInfo connectionMethod;
    public String window;
    public ConnectionStrategyInfo connectionStrategy;
    public Instant nextExecution;
    public long comSessionId;

    public static class LatestStatusInfo {
        @XmlJavaTypeAdapter(ConnectionTaskSuccessIndicatorAdapter.class)
        public ConnectionTask.SuccessIndicator id;
        public String displayValue;
    }

    public static class ConnectionStrategyInfo {
        @XmlJavaTypeAdapter(ConnectionStrategyAdapter.class)
        public ConnectionStrategy id;
        public String displayValue;
    }

    public static class ComTaskCountInfo {
        public long numberOfSuccessfulTasks;
        public long numberOfFailedTasks;
        public long numberOfIncompleteTasks;
    }

    public static class ConnectionMethodInfo extends IdWithNameInfo {
        public boolean isDefault;
        @XmlJavaTypeAdapter(ConnectionTaskLifecycleStatusAdapter.class)
        public ConnectionTaskLifecycleStatus status;
    }
}
