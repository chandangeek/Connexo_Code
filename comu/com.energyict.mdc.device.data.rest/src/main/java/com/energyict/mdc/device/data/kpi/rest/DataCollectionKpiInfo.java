package com.energyict.mdc.device.data.kpi.rest;

import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Created by bvn on 12/12/14.
 */
public class DataCollectionKpiInfo {
    public Long id;
    public IdWithNameInfo deviceGroup;
    public TemporalExpressionInfo frequency;
    public BigDecimal connectionTarget;
    public BigDecimal communicationTarget;
    public Instant latestCalculationDate;
}
