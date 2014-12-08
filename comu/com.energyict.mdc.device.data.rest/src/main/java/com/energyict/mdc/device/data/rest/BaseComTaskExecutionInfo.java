package com.energyict.mdc.device.data.rest;

import java.util.Date;
import java.util.List;

import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

public class BaseComTaskExecutionInfo {

    public String name;
    public List<IdWithNameInfo> comTasks;
    public String comScheduleName;
    public TemporalExpressionInfo comScheduleFrequency;
    public int urgency;
    public TaskStatusInfo currentState;
    public CompletionCodeInfo latestResult;
    public Date startTime;
    public Date successfulFinishTime;
    public Date nextCommunication;

}