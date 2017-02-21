/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.kpi.rest;

import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

/**
 * Created by bvn on 12/12/14.
 */
public class DataCollectionKpiInfoFactory {

    public DataCollectionKpiInfo from(DataCollectionKpi kpi) {
        DataCollectionKpiInfo kpiInfo = new DataCollectionKpiInfo();
        kpiInfo.id = kpi.getId();
        kpiInfo.deviceGroup = new LongIdWithNameInfo(kpi.getDeviceGroup().getId(), kpi.getDeviceGroup().getName());
        kpiInfo.displayRange = new TimeDurationInfo(kpi.getDisplayRange());
        kpiInfo.frequency = kpi.getFrequency()!=null?TemporalExpressionInfo.from(kpi.getFrequency()):null;
        kpiInfo.version = kpi.getVersion();
        kpi.getStaticCommunicationKpiTarget().ifPresent(target -> kpiInfo.communicationTarget = target);
        kpi.getStaticConnectionKpiTarget().ifPresent(target -> kpiInfo.connectionTarget = target);
        kpi.getLatestCalculation().ifPresent(target -> kpiInfo.latestCalculationDate = target);
        return kpiInfo;
    }

}
