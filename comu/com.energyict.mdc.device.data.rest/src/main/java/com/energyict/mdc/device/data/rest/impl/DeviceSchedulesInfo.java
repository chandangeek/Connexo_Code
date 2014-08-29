package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import com.energyict.mdc.scheduling.rest.ComTaskInfo;
import com.energyict.mdc.tasks.ComTask;

import java.util.*;

public class DeviceSchedulesInfo {
    public long id;
    public long masterScheduleId;
    public String name;
    public TemporalExpressionInfo schedule;
    public Date plannedDate;
    public List<ComTaskInfo> comTaskInfos;
    public ScheduleType type;

    public DeviceSchedulesInfo() {
    }

    public static List<DeviceSchedulesInfo> from(List<ComTaskExecution> comTaskExecutions, List<ComTaskEnablement> comTaskEnablements) {
        List<DeviceSchedulesInfo> deviceSchedulesInfos = new ArrayList<>();
        Set<Long> usedComtaskIds = new HashSet<>();
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            for (ComTask comTask : comTaskExecution.getComTasks()) {
                if (!comTaskExecution.isAdHoc()) {
                    usedComtaskIds.add(comTask.getId());
                }
            }
        }
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            if(comTaskExecution.isScheduledManually()){
                deviceSchedulesInfos.add(DeviceSchedulesInfo.fromManual(comTaskExecution));
            }  else if(comTaskExecution.isScheduled()){
                deviceSchedulesInfos.add(DeviceSchedulesInfo.fromScheduled(comTaskExecution));
            } else if(comTaskExecution.isAdHoc()){
                if(!usedComtaskIds.contains(comTaskExecution.getComTasks().get(0).getId())){
                    deviceSchedulesInfos.add(DeviceSchedulesInfo.fromAdHoc(comTaskExecution));
                }
            }
        }
        deviceSchedulesInfos.addAll(DeviceSchedulesInfo.fromEnablements(comTaskEnablements, usedComtaskIds));
        return deviceSchedulesInfos;
    }

    private static List<DeviceSchedulesInfo> fromEnablements(List<ComTaskEnablement> comTaskEnablements, Set<Long> usedComtasks) {
        List<DeviceSchedulesInfo> deviceSchedulesInfos = new ArrayList<>();
        for(ComTaskEnablement comTaskEnablement : comTaskEnablements){
            if(!usedComtasks.contains(comTaskEnablement.getComTask().getId())){
                DeviceSchedulesInfo deviceSchedulesInfo = new DeviceSchedulesInfo();
                deviceSchedulesInfo.id = comTaskEnablement.getComTask().getId();
                deviceSchedulesInfo.comTaskInfos = new ArrayList<>();
                deviceSchedulesInfo.comTaskInfos.addAll(Arrays.asList(ComTaskInfo.from(comTaskEnablement.getComTask())));
                deviceSchedulesInfo.type = ScheduleType.ONREQUEST;
                deviceSchedulesInfos.add(deviceSchedulesInfo);
            }
        }
        return deviceSchedulesInfos;
    }

    public static DeviceSchedulesInfo fromScheduled(ComTaskExecution comTaskExecution) {
        DeviceSchedulesInfo deviceSchedulesInfo = new DeviceSchedulesInfo();
        deviceSchedulesInfo.id = comTaskExecution.getId();
        deviceSchedulesInfo.masterScheduleId = ((ScheduledComTaskExecution)comTaskExecution).getComSchedule().getId();
        deviceSchedulesInfo.name =((ScheduledComTaskExecution)comTaskExecution).getComSchedule().getName();
        deviceSchedulesInfo.schedule = TemporalExpressionInfo.from(comTaskExecution.getNextExecutionSpecs().get().getTemporalExpression());
        deviceSchedulesInfo.plannedDate = comTaskExecution.getPlannedNextExecutionTimestamp();
        deviceSchedulesInfo.comTaskInfos = new ArrayList<>();
        deviceSchedulesInfo.comTaskInfos.addAll(ComTaskInfo.from(comTaskExecution.getComTasks()));
        deviceSchedulesInfo.type = ScheduleType.SCHEDULED;
        return deviceSchedulesInfo;
    }

    public static DeviceSchedulesInfo fromManual(ComTaskExecution comTaskExecution) {
        DeviceSchedulesInfo deviceSchedulesInfo = new DeviceSchedulesInfo();
        deviceSchedulesInfo.id = comTaskExecution.getId();
        deviceSchedulesInfo.type = ScheduleType.INDIVIDUAL;
        deviceSchedulesInfo.schedule = TemporalExpressionInfo.from(comTaskExecution.getNextExecutionSpecs().get().getTemporalExpression());
        deviceSchedulesInfo.plannedDate = comTaskExecution.getPlannedNextExecutionTimestamp();
        deviceSchedulesInfo.comTaskInfos = new ArrayList<>();
        deviceSchedulesInfo.comTaskInfos.addAll(ComTaskInfo.from(comTaskExecution.getComTasks()));
        return deviceSchedulesInfo;
    }

    public static DeviceSchedulesInfo fromAdHoc(ComTaskExecution comTaskExecution) {
        DeviceSchedulesInfo deviceSchedulesInfo = new DeviceSchedulesInfo();
        deviceSchedulesInfo.id = comTaskExecution.getId();
        deviceSchedulesInfo.type = ScheduleType.ADHOC;
        deviceSchedulesInfo.plannedDate = comTaskExecution.getNextExecutionTimestamp();
        deviceSchedulesInfo.comTaskInfos = new ArrayList<>();
        deviceSchedulesInfo.comTaskInfos.addAll(ComTaskInfo.from(comTaskExecution.getComTasks()));
        return deviceSchedulesInfo;
    }

    private enum ScheduleType {
        ONREQUEST,SCHEDULED,INDIVIDUAL,ADHOC;
    }

}
