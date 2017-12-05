/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.kpi.rest;

import com.elster.jupiter.rest.util.LongIdWithNameInfo;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import java.util.List;
import java.util.stream.Collectors;

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
        kpi.communicationKpiTask().ifPresent(task -> {
            kpiInfo.communicationTaskId = task.getId();
            kpiInfo.communicationNextRecurrentTasks = constructTaskInfo(task.getNextRecurrentTasks());
            kpiInfo.communicationPreviousRecurrentTasks = constructTaskInfo(task.getPrevRecurrentTasks());
        });
        kpi.connectionKpiTask().ifPresent(task -> {
            kpiInfo.connectionTaskId = task.getId();
            kpiInfo.connectionNextRecurrentTasks = constructTaskInfo(task.getNextRecurrentTasks());
            kpiInfo.connectionPreviousRecurrentTasks = constructTaskInfo(task.getPrevRecurrentTasks());
        });
        return kpiInfo;
    }

    private List<TaskInfo> constructTaskInfo(List<RecurrentTask> recurrentTasks) {
        return recurrentTasks.stream().map(recurrentTask -> TaskInfo.from(recurrentTask)).collect(Collectors.toList());
    }
}
