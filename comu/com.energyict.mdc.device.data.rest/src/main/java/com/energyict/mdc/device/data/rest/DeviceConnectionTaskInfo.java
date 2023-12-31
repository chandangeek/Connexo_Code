/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.energyict.mdc.common.tasks.ConnectionTask.ConnectionTaskLifecycleStatus;
import com.energyict.mdc.device.data.rest.impl.ConnectionTaskVersionInfo;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;

public class DeviceConnectionTaskInfo extends ConnectionTaskVersionInfo {
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
    public ConnectionStrategyInfo connectionStrategyInfo;
    public Instant nextExecution;
    public long comSessionId;
    public String protocolDialect;
    public String protocolDialectDisplayName;

    public static class LatestStatusInfo {
        public String id;
        public String displayValue;
    }

    public static class ConnectionStrategyInfo {
        public String connectionStrategy;
        public String localizedValue;
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
