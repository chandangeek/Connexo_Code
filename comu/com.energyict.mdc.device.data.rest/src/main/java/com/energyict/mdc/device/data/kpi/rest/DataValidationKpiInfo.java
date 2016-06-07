package com.energyict.mdc.device.data.kpi.rest;


import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import java.time.Instant;

public class DataValidationKpiInfo {

    public Long id;
    public LongIdWithNameInfo deviceGroup;
    public TemporalExpressionInfo frequency;
    public Instant latestCalculationDate;
    public long version;

}
