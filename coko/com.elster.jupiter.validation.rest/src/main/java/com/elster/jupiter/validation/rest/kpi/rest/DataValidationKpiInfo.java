/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.rest.kpi.rest;

import java.time.Instant;

public class DataValidationKpiInfo {

    public Long id;
    public LongIdWithNameInfo deviceGroup;
    public TemporalExpressionInfo frequency;
    public Instant latestCalculationDate;
    public long version;

}

