package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.scheduling.rest.ComTaskInfo;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import com.energyict.mdc.tasks.ComTask;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DeviceSchedulesInfo {
    public long id; // identifies either comtask or comtaskexecution, dependening on context!!!
    public long masterScheduleId;
    public String name;
    public TemporalExpressionInfo schedule;
    public Instant plannedDate;
    public Instant nextCommunication;
    public List<ComTaskInfo> comTaskInfos;
    public ScheduleType type;
    public long version;
    public VersionInfo<String> parent;

    public DeviceSchedulesInfo() {
    }

    public static List<DeviceSchedulesInfo> from(List<ComTaskExecution> comTaskExecutions, List<ComTaskEnablement> comTaskEnablements) {
        List<DeviceSchedulesInfo> deviceSchedulesInfos = new ArrayList<>();
        Set<Long> usedComtaskIds =
                comTaskExecutions
                        .stream()
                        .flatMap(each -> each.getComTasks().stream())
                        .map(ComTask::getId)
                        .collect(Collectors.toSet());
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            if (comTaskExecution.isScheduledManually() && !comTaskExecution.isAdHoc()) {
                deviceSchedulesInfos.add(DeviceSchedulesInfo.fromManual(comTaskExecution));
            }  else if(comTaskExecution.usesSharedSchedule()){
                deviceSchedulesInfos.add(DeviceSchedulesInfo.fromScheduled(comTaskExecution));
            } else if(comTaskExecution.isAdHoc()){
                    deviceSchedulesInfos.add(DeviceSchedulesInfo.fromAdHoc(comTaskExecution));
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
        deviceSchedulesInfo.nextCommunication = comTaskExecution.getNextExecutionTimestamp();
        deviceSchedulesInfo.comTaskInfos = new ArrayList<>();
        deviceSchedulesInfo.comTaskInfos.addAll(ComTaskInfo.from(comTaskExecution.getComTasks()));
        deviceSchedulesInfo.type = ScheduleType.SCHEDULED;
        deviceSchedulesInfo.version = comTaskExecution.getVersion();
        Device device = comTaskExecution.getDevice();
        deviceSchedulesInfo.parent = new VersionInfo<>(device.getName(), device.getVersion());
        return deviceSchedulesInfo;
    }

    public static DeviceSchedulesInfo fromManual(ComTaskExecution comTaskExecution) {
        DeviceSchedulesInfo deviceSchedulesInfo = new DeviceSchedulesInfo();
        deviceSchedulesInfo.id = comTaskExecution.getId();
        deviceSchedulesInfo.type = ScheduleType.INDIVIDUAL;
        deviceSchedulesInfo.schedule = TemporalExpressionInfo.from(comTaskExecution.getNextExecutionSpecs().get().getTemporalExpression());
        deviceSchedulesInfo.plannedDate = comTaskExecution.getPlannedNextExecutionTimestamp();
        deviceSchedulesInfo.nextCommunication = comTaskExecution.getNextExecutionTimestamp();
        deviceSchedulesInfo.comTaskInfos = new ArrayList<>();
        deviceSchedulesInfo.comTaskInfos.addAll(ComTaskInfo.from(comTaskExecution.getComTasks()));
        deviceSchedulesInfo.version = comTaskExecution.getVersion();
        Device device = comTaskExecution.getDevice();
        deviceSchedulesInfo.parent = new VersionInfo<>(device.getName(), device.getVersion());
        return deviceSchedulesInfo;
    }

    public static DeviceSchedulesInfo fromAdHoc(ComTaskExecution comTaskExecution) {
        DeviceSchedulesInfo deviceSchedulesInfo = new DeviceSchedulesInfo();
        deviceSchedulesInfo.id = comTaskExecution.getId();
        deviceSchedulesInfo.type = ScheduleType.ADHOC;
        deviceSchedulesInfo.plannedDate = comTaskExecution.getNextExecutionTimestamp();
        deviceSchedulesInfo.nextCommunication = comTaskExecution.getNextExecutionTimestamp();
        deviceSchedulesInfo.comTaskInfos = new ArrayList<>();
        deviceSchedulesInfo.comTaskInfos.addAll(ComTaskInfo.from(comTaskExecution.getComTasks()));
        deviceSchedulesInfo.version = comTaskExecution.getVersion();
        Device device = comTaskExecution.getDevice();
        deviceSchedulesInfo.parent = new VersionInfo<>(device.getName(), device.getVersion());
        return deviceSchedulesInfo;
    }

    private enum ScheduleType {
        ONREQUEST,SCHEDULED,INDIVIDUAL,ADHOC;
    }

}
