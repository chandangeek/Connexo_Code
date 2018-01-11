/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.kpi.rest.impl;

import com.elster.jupiter.rest.util.LongIdWithNameInfo;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import java.time.Instant;
import java.util.List;

public class RegisteredDevicesKpiInfo {
    public Long id;
    public LongIdWithNameInfo deviceGroup;
    public TemporalExpressionInfo frequency;
    public long target;
    public Instant latestCalculationDate;
    public long version;
    public List<TaskInfo> nextRecurrentTasks;
    public List<TaskInfo> previousRecurrentTasks;
}
