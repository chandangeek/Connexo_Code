/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.kpi.rest;

import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Created by bvn on 12/12/14.
 */
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


