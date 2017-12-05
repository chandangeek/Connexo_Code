/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.kpi.rest;

import com.elster.jupiter.rest.util.LongIdWithNameInfo;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class DataCollectionKpiInfo {

    public Long id;
    public LongIdWithNameInfo deviceGroup;
    public TemporalExpressionInfo frequency;
    public TimeDurationInfo displayRange;
    public BigDecimal connectionTarget;
    public BigDecimal communicationTarget;
    public Instant latestCalculationDate;
    public Long connectionTaskId;
    public Long communicationTaskId;
    public long version;
    public List<TaskInfo> connectionNextRecurrentTasks;
    public List<TaskInfo> communicationNextRecurrentTasks;
    public List<TaskInfo> connectionPreviousRecurrentTasks;
    public List<TaskInfo> communicationPreviousRecurrentTasks;
}


