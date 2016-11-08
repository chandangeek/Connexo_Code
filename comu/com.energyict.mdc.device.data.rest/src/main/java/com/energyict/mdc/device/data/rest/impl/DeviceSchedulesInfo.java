package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.scheduling.rest.ComTaskInfo;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import java.time.Instant;
import java.util.ArrayList;
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
    public ComTaskInfo comTask;
    public ScheduleType type;
    public long version;
    public VersionInfo<String> parent;
    public boolean active;
    public boolean hasConnectionWindow;
    public String connectionStrategyKey;

    public DeviceSchedulesInfo() {
    }

    public static List<DeviceSchedulesInfo> from(List<ComTaskExecution> comTaskExecutions, List<ComTaskEnablement> comTaskEnablements) {
        List<DeviceSchedulesInfo> deviceSchedulesInfos = new ArrayList<>();
        Set<Long> usedComtaskIds =
                comTaskExecutions
                        .stream()
                        .map(each -> each.getComTask().getId())
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
                deviceSchedulesInfos.add(fromEnablement(comTaskEnablement));
            }
        }
        return deviceSchedulesInfos;
    }

    public static DeviceSchedulesInfo fromEnablement(ComTaskEnablement comTaskEnablement) {
        DeviceSchedulesInfo deviceSchedulesInfo = new DeviceSchedulesInfo();
        deviceSchedulesInfo.id = comTaskEnablement.getComTask().getId();
        deviceSchedulesInfo.comTask = ComTaskInfo.from(comTaskEnablement.getComTask());
        deviceSchedulesInfo.active = !comTaskEnablement.isSuspended();
        deviceSchedulesInfo.type = ScheduleType.ONREQUEST;
        return deviceSchedulesInfo;
    }

    public static DeviceSchedulesInfo fromScheduled(ComTaskExecution comTaskExecution) {
        DeviceSchedulesInfo deviceSchedulesInfo = new DeviceSchedulesInfo();
        deviceSchedulesInfo.id = comTaskExecution.getId();
        deviceSchedulesInfo.masterScheduleId = ((ScheduledComTaskExecution)comTaskExecution).getComSchedule().getId();
        deviceSchedulesInfo.name =((ScheduledComTaskExecution)comTaskExecution).getComSchedule().getName();
        deviceSchedulesInfo.schedule = TemporalExpressionInfo.from(comTaskExecution.getNextExecutionSpecs().get().getTemporalExpression());
        deviceSchedulesInfo.plannedDate = comTaskExecution.getPlannedNextExecutionTimestamp();
        deviceSchedulesInfo.nextCommunication = comTaskExecution.getNextExecutionTimestamp();
        deviceSchedulesInfo.comTask = ComTaskInfo.from(comTaskExecution.getComTask());
        deviceSchedulesInfo.type = ScheduleType.SCHEDULED;
        deviceSchedulesInfo.version = comTaskExecution.getVersion();
        deviceSchedulesInfo.active = !comTaskExecution.isOnHold();
        deviceSchedulesInfo.hasConnectionWindow = hasCommunicationWindow(comTaskExecution);
        deviceSchedulesInfo.connectionStrategyKey = getConnectionStrategyKey(comTaskExecution);
        Device device = comTaskExecution.getDevice();
        deviceSchedulesInfo.parent = new VersionInfo<>(device.getmRID(), device.getVersion());
        return deviceSchedulesInfo;
    }

    public static DeviceSchedulesInfo fromManual(ComTaskExecution comTaskExecution) {
        DeviceSchedulesInfo deviceSchedulesInfo = new DeviceSchedulesInfo();
        deviceSchedulesInfo.id = comTaskExecution.getId();
        deviceSchedulesInfo.type = ScheduleType.INDIVIDUAL;
        deviceSchedulesInfo.schedule = TemporalExpressionInfo.from(comTaskExecution.getNextExecutionSpecs().get().getTemporalExpression());
        deviceSchedulesInfo.plannedDate = comTaskExecution.getPlannedNextExecutionTimestamp();
        deviceSchedulesInfo.nextCommunication = comTaskExecution.getNextExecutionTimestamp();
        deviceSchedulesInfo.comTask = ComTaskInfo.from(comTaskExecution.getComTask());
        deviceSchedulesInfo.version = comTaskExecution.getVersion();
        deviceSchedulesInfo.active = !comTaskExecution.isOnHold();
        deviceSchedulesInfo.hasConnectionWindow = hasCommunicationWindow(comTaskExecution);
        deviceSchedulesInfo.connectionStrategyKey = getConnectionStrategyKey(comTaskExecution);
        Device device = comTaskExecution.getDevice();
        deviceSchedulesInfo.parent = new VersionInfo<>(device.getmRID(), device.getVersion());
        return deviceSchedulesInfo;
    }

    public static DeviceSchedulesInfo fromAdHoc(ComTaskExecution comTaskExecution) {
        DeviceSchedulesInfo deviceSchedulesInfo = new DeviceSchedulesInfo();
        deviceSchedulesInfo.id = comTaskExecution.getId();
        deviceSchedulesInfo.type = ScheduleType.ADHOC;
        deviceSchedulesInfo.plannedDate = comTaskExecution.getNextExecutionTimestamp();
        deviceSchedulesInfo.nextCommunication = comTaskExecution.getNextExecutionTimestamp();
        deviceSchedulesInfo.comTask = ComTaskInfo.from(comTaskExecution.getComTask());
        deviceSchedulesInfo.version = comTaskExecution.getVersion();
        deviceSchedulesInfo.active = !comTaskExecution.isOnHold();
        deviceSchedulesInfo.hasConnectionWindow = hasCommunicationWindow(comTaskExecution);
        deviceSchedulesInfo.connectionStrategyKey = getConnectionStrategyKey(comTaskExecution);
        Device device = comTaskExecution.getDevice();
        deviceSchedulesInfo.parent = new VersionInfo<>(device.getmRID(), device.getVersion());
        return deviceSchedulesInfo;
    }

    private static boolean hasCommunicationWindow(ComTaskExecution comTaskExecution) {
        if(comTaskExecution.getConnectionTask().isPresent()) {
            if(comTaskExecution.getConnectionTask().get() instanceof ScheduledConnectionTask) {
                ScheduledConnectionTask task = (ScheduledConnectionTask) comTaskExecution.getConnectionTask().get();
                return task.getCommunicationWindow() != null;
            }
        }
        return false;
    }

    private static String getConnectionStrategyKey(ComTaskExecution comTaskExecution) {
        if(comTaskExecution.getConnectionTask().isPresent()) {
            if(comTaskExecution.getConnectionTask().get() instanceof ScheduledConnectionTask) {
                ScheduledConnectionTask task = (ScheduledConnectionTask) comTaskExecution.getConnectionTask().get();
                return task.getConnectionStrategy().name();
            }
        }
        return null;
    }


    private enum ScheduleType {
        ONREQUEST,SCHEDULED,INDIVIDUAL,ADHOC;
    }

}
