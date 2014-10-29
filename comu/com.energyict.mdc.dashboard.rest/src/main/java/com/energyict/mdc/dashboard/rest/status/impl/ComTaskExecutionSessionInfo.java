package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.configuration.rest.DeviceConfigurationIdInfo;
import com.energyict.mdc.device.data.rest.CompletionCodeInfo;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import java.util.Date;
import java.util.List;

/**
 * Created by bvn on 8/12/14.
 */
public class ComTaskExecutionSessionInfo {

    public String name;
    public long id;
    public List<IdWithNameInfo> comTasks;
    public IdWithNameInfo device;
    public DeviceConfigurationIdInfo deviceConfiguration;
    public IdWithNameInfo deviceType;
    public String comScheduleName;
    public TemporalExpressionInfo comScheduleFrequency;
    public int urgency;
    public TaskStatusInfo currentState;
    public CompletionCodeInfo latestResult;
    public Date startTime;
    public Date successfulFinishTime;
    public Date nextCommunication;
    public boolean alwaysExecuteOnInbound;


}
