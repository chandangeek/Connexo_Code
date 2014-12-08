package com.energyict.mdc.device.data.rest;

import java.util.Date;

import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

public class BaseComTaskExecutionInfo {

    public String name;
    public String comScheduleName;
    public TemporalExpressionInfo comScheduleFrequency;
    public int urgency;
    public TaskStatusInfo currentState;
    public CompletionCodeInfo latestResult;
    public Date startTime;
    public Date successfulFinishTime;
    public Date nextCommunication;

}