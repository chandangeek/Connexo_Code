/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.kpi.rest;

import com.elster.jupiter.rest.util.LongIdWithNameInfo;
import com.elster.jupiter.tasks.RecurrentTask;
import com.energyict.mdc.device.data.kpi.rest.impl.RegisteredDevicesKpiInfo;
import com.energyict.mdc.device.data.kpi.rest.impl.TaskInfo;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpi;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import java.util.List;
import java.util.stream.Collectors;

public class RegisteredDevicesKpiInfoFactory {

    public RegisteredDevicesKpiInfo from(RegisteredDevicesKpi kpi) {
        RegisteredDevicesKpiInfo kpiInfo = simplified(kpi);
        kpiInfo.frequency = kpi.getFrequency()!=null?TemporalExpressionInfo.from(kpi.getFrequency()):null;
        kpiInfo.version = kpi.getVersion();
        kpiInfo.target = kpi.getTarget();
        kpiInfo.nextRecurrentTasks = constructTaskInfo(kpi.getNextRecurrentTasks());
        kpiInfo.previousRecurrentTasks = constructTaskInfo(kpi.getPrevRecurrentTasks());
        kpi.getLatestCalculation().ifPresent(target -> kpiInfo.latestCalculationDate = target);
        return kpiInfo;
    }

    public RegisteredDevicesKpiInfo simplified(RegisteredDevicesKpi kpi) {
        RegisteredDevicesKpiInfo kpiInfo = new RegisteredDevicesKpiInfo();
        kpiInfo.id = kpi.getId();
        kpiInfo.deviceGroup = new LongIdWithNameInfo(kpi.getDeviceGroup().getId(), kpi.getDeviceGroup().getName());
        return kpiInfo;
    }

    private List<TaskInfo> constructTaskInfo(List<RecurrentTask> recurrentTasks) {
        return recurrentTasks.stream().map(recurrentTask -> TaskInfo.from(recurrentTask)).collect(Collectors.toList());
    }


}
