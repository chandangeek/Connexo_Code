package com.energyict.mdc.device.data.rest;

import java.time.Instant;

import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

public class BaseComTaskExecutionInfo {

    public String name;
    public String comScheduleName;
    public TemporalExpressionInfo comScheduleFrequency;
    public int urgency;
    public TaskStatusInfo currentState;
    public CompletionCodeInfo latestResult;
    public Instant startTime;
    public Instant successfulFinishTime;
    public Instant nextCommunication;

}