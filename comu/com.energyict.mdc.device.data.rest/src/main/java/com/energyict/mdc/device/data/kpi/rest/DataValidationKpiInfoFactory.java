package com.energyict.mdc.device.data.kpi.rest;


import com.energyict.mdc.device.data.kpi.DataValidationKpi;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

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
