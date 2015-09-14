package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.configuration.rest.DeviceConfigurationIdInfo;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import java.time.Instant;

/**
 * Created by bvn on 8/12/14.
 */
public class ComTaskExecutionSessionInfo {

    public String name;
    public long id;
    public IdWithNameInfo comTask;
    public IdWithNameInfo device;
    public DeviceConfigurationIdInfo deviceConfiguration;
    public IdWithNameInfo deviceType;
    public String comScheduleName;
    public TemporalExpressionInfo comScheduleFrequency;
    public int urgency;
    public TaskStatusInfo currentState;
    public CompletionCodeInfo result;
    public Instant startTime;
    public Instant stopTime;
    public Instant nextCommunication;
    public boolean alwaysExecuteOnInbound;

}