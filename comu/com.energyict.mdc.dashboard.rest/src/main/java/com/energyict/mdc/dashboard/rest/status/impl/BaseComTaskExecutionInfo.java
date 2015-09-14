package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import java.time.Instant;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-04 (16:31)
 */
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