/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.kpi.rest;

import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import java.math.BigDecimal;
import java.time.Instant;

public class DataCollectionKpiInfo {

    public Long id;
    public LongIdWithNameInfo deviceGroup;
    public TemporalExpressionInfo frequency;
    public TimeDurationInfo displayRange;
    public BigDecimal connectionTarget;
    public BigDecimal communicationTarget;
    public Instant latestCalculationDate;
    public long version;
}


