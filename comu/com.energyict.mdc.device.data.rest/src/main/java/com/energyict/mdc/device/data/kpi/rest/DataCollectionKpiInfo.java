package com.energyict.mdc.device.data.kpi.rest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.TemporalAmount;

/**
 * Created by bvn on 12/12/14.
 */
public class DataCollectionKpiInfo {
    public String deviceGroup;
    public TemporalAmount frequency;
    public BigDecimal connectionTarget;
    public BigDecimal communicationTarget;
    public Instant latestCalculationDate;
}
