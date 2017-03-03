/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.rest.impl;

import com.elster.jupiter.rest.util.LongIdWithNameInfo;

import java.time.Instant;

public abstract class DataQualityKpiInfo {

    public Long id;
    public TemporalExpressionInfo frequency;
    public Instant latestCalculationDate;
    public long version;

    public static class DeviceDataQualityKpiInfo extends DataQualityKpiInfo {

        public LongIdWithNameInfo deviceGroup;

    }

    public static class UsagePointDataQualityKpiInfo extends DataQualityKpiInfo {

        public LongIdWithNameInfo usagePointGroup;
        public LongIdWithNameInfo metrologyPurpose;

        public LongIdWithNameInfo[] purposes;// used only to POST kpis for multiple purposes at once

    }
}
