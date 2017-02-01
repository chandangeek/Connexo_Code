/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.rest.kpi.rest;


import com.elster.jupiter.validation.kpi.DataValidationKpi;

public class DataValidationKpiInfoFactory {

    public DataValidationKpiInfo from(DataValidationKpi kpi) {
        DataValidationKpiInfo kpiInfo = new DataValidationKpiInfo();
        kpiInfo.id = kpi.getId();
        kpiInfo.deviceGroup = new LongIdWithNameInfo(kpi.getDeviceGroup().getId(), kpi.getDeviceGroup().getName());
        kpiInfo.frequency = kpi.getFrequency()!=null? TemporalExpressionInfo.from(kpi.getFrequency()):null;
        kpiInfo.version = kpi.getVersion();
        kpi.getLatestCalculation().ifPresent(target -> kpiInfo.latestCalculationDate = target);
        return kpiInfo;
    }


}
