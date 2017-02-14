/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.rest.kpi.rest;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.validation.kpi.DataValidationKpi;
import com.elster.jupiter.validation.kpi.EndDeviceDataQuality;
import com.elster.jupiter.validation.kpi.UsagePointDataQuality;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "dataValidationKpi")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DataQualityKpiInfo.UsagePointDataQualityKpiInfo.class, name = "usagePointDataQualityKpiInfo"),
        @JsonSubTypes.Type(value = DataQualityKpiInfo.DeviceDataQualityKpiInfo.class, name = "deviceDataQualityKpiInfo"),
})
public abstract class DataQualityKpiInfo {

    public Long id;
    public TemporalExpressionInfo frequency;
    public Instant latestCalculationDate;
    public long version;

    public abstract DataValidationKpi createNew(DataValidationKpiInfoFactory factory);

    static class UsagePointDataQualityKpiInfo extends DataQualityKpiInfo {
        public LongIdWithNameInfo usagePointGroup;
        public IdWithNameInfo purpose;

        public UsagePointDataQuality createNew(DataValidationKpiInfoFactory factory) {
            return factory.createNewKpi(this);
        }
    }

    static class DeviceDataQualityKpiInfo extends DataQualityKpiInfo {
        public LongIdWithNameInfo deviceGroup;

        public EndDeviceDataQuality createNew(DataValidationKpiInfoFactory factory) {
            return factory.createNewKpi(this);
        }
    }
}

