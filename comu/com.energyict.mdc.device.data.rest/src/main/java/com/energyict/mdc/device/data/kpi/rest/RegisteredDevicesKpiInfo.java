/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.kpi.rest;

import com.elster.jupiter.rest.util.LongIdWithNameInfo;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import java.time.Instant;

public class RegisteredDevicesKpiInfo {
    public Long id;
    public LongIdWithNameInfo deviceGroup;
    public TemporalExpressionInfo frequency;
    public int target;
    public Instant latestCalculationDate;
    public long version;
}
