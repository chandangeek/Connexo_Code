/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.kpi.rest;

import com.elster.jupiter.rest.util.LongIdWithNameInfo;
import com.energyict.mdc.device.data.kpi.rest.impl.RegisteredDevicesKpiInfo;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpi;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

public class RegisteredDevicesKpiInfoFactory {

    public RegisteredDevicesKpiInfo from(RegisteredDevicesKpi kpi) {
        RegisteredDevicesKpiInfo kpiInfo = simplified(kpi);
        kpiInfo.frequency = kpi.getFrequency()!=null?TemporalExpressionInfo.from(kpi.getFrequency()):null;
        kpiInfo.version = kpi.getVersion();
        kpiInfo.target = kpi.getTarget();
        kpi.getLatestCalculation().ifPresent(target -> kpiInfo.latestCalculationDate = target);
        return kpiInfo;
    }

    public RegisteredDevicesKpiInfo simplified(RegisteredDevicesKpi kpi) {
        RegisteredDevicesKpiInfo kpiInfo = new RegisteredDevicesKpiInfo();
        kpiInfo.id = kpi.getId();
        kpiInfo.deviceGroup = new LongIdWithNameInfo(kpi.getDeviceGroup().getId(), kpi.getDeviceGroup().getName());
        return kpiInfo;
    }

}
