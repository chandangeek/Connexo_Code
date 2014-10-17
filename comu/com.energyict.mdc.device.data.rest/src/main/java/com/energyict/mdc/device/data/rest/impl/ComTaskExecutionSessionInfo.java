package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import java.util.Date;
import java.util.List;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-17 (16:34)
 */
class ComTaskExecutionSessionInfo {
    public String name;
    public List<IdWithNameInfo> comTasks;
    public IdWithNameInfo device;
    public IdWithNameInfo deviceConfiguration;
    public IdWithNameInfo deviceType;
    public String comScheduleName;
    public TemporalExpressionInfo comScheduleFrequency;
    public int urgency;
    public String result;
    public Date startTime;
    public Date finishTime;
    public boolean alwaysExecuteOnInbound;
}