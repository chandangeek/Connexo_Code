/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.device.configuration.rest.DeviceConfigurationIdInfo;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import java.time.Instant;
import java.util.List;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-17 (16:34)
 */
class ComTaskExecutionSessionInfo {
    public long id;
    public String name;
    public List<IdWithNameInfo> comTasks;
    public IdWithNameInfo device;
    public DeviceConfigurationIdInfo deviceConfiguration;
    public IdWithNameInfo deviceType;
    public String comScheduleName;
    public TemporalExpressionInfo comScheduleFrequency;
    public int urgency;
    public String result;
    public Instant startTime;
    public Instant finishTime;
    public Long durationInSeconds;
    public boolean alwaysExecuteOnInbound;
    public ComSessionInfo comSession;
}