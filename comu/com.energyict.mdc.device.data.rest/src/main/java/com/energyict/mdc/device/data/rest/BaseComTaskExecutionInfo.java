/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest;

import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import java.time.Instant;

public class BaseComTaskExecutionInfo {

    public long id;
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