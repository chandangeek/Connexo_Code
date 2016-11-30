package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.scheduling.rest.ComTaskInfo;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    public boolean connectionDefinedOnDevice;

    public DeviceSchedulesInfo() {
    }

    public static List<DeviceSchedulesInfo> from(List<ComTaskExecution> comTaskExecutions, List<ComTaskEnablement> comTaskEnablements, Device device) {
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
        deviceSchedulesInfos.addAll(DeviceSchedulesInfo.fromEnablements(comTaskEnablements, usedComtaskIds, device));
        return deviceSchedulesInfos;
    }

    private static List<DeviceSchedulesInfo> fromEnablements(List<ComTaskEnablement> comTaskEnablements, Set<Long> usedComtasks, Device device) {
        List<DeviceSchedulesInfo> deviceSchedulesInfos = new ArrayList<>();
        for(ComTaskEnablement comTaskEnablement : comTaskEnablements){
            if(!usedComtasks.contains(comTaskEnablement.getComTask().getId())){
                deviceSchedulesInfos.add(fromEnablement(comTaskEnablement, device));
            }
        }
        return deviceSchedulesInfos;
    }

    public static DeviceSchedulesInfo fromEnablement(ComTaskEnablement comTaskEnablement, Device device) {
        DeviceSchedulesInfo deviceSchedulesInfo = new DeviceSchedulesInfo();
        deviceSchedulesInfo.id = comTaskEnablement.getComTask().getId();
        deviceSchedulesInfo.comTask = ComTaskInfo.from(comTaskEnablement.getComTask());
        deviceSchedulesInfo.active = !comTaskEnablement.isSuspended();
        deviceSchedulesInfo.type = ScheduleType.ONREQUEST;
        if (comTaskEnablement.getPartialConnectionTask().isPresent()) {
            PartialConnectionTask partialConnectionTask = comTaskEnablement.getPartialConnectionTask().get();
            Optional<ConnectionTask<?, ?>> deviceConnectionTaskOptional = device.getConnectionTasks().stream()
                    .filter(connectionTask -> connectionTask.getName().equals(partialConnectionTask.getName())).findFirst();
            deviceSchedulesInfo.connectionDefinedOnDevice = deviceConnectionTaskOptional.isPresent();

            if (partialConnectionTask instanceof PartialScheduledConnectionTask) {
                ConnectionStrategy connectionStrategy = ((PartialScheduledConnectionTask) partialConnectionTask).getConnectionStrategy();
                deviceSchedulesInfo.connectionStrategyKey = connectionStrategy.name();
            }
        }
        deviceSchedulesInfo.parent = new VersionInfo<>(device.getName(), device.getVersion());
        return deviceSchedulesInfo;
    }

    public static DeviceSchedulesInfo fromScheduled(ComTaskExecution comTaskExecution) {
        DeviceSchedulesInfo deviceSchedulesInfo = new DeviceSchedulesInfo();
        deviceSchedulesInfo.id = comTaskExecution.getId();
        deviceSchedulesInfo.masterScheduleId = comTaskExecution.getComSchedule().get().getId();
        deviceSchedulesInfo.name =comTaskExecution.getComSchedule().get().getName();
        deviceSchedulesInfo.schedule = TemporalExpressionInfo.from(comTaskExecution.getNextExecutionSpecs().get().getTemporalExpression());
        deviceSchedulesInfo.plannedDate = comTaskExecution.getPlannedNextExecutionTimestamp();
        deviceSchedulesInfo.nextCommunication = comTaskExecution.getNextExecutionTimestamp();
        deviceSchedulesInfo.comTask = ComTaskInfo.from(comTaskExecution.getComTask());
        deviceSchedulesInfo.type = ScheduleType.SCHEDULED;
        deviceSchedulesInfo.version = comTaskExecution.getVersion();
        deviceSchedulesInfo.active = !comTaskExecution.isOnHold();
        deviceSchedulesInfo.hasConnectionWindow = hasCommunicationWindow(comTaskExecution);
        setConnectionTaskInfo(comTaskExecution, deviceSchedulesInfo);
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
        deviceSchedulesInfo.comTask = ComTaskInfo.from(comTaskExecution.getComTask());
        deviceSchedulesInfo.version = comTaskExecution.getVersion();
        deviceSchedulesInfo.active = !comTaskExecution.isOnHold();
        deviceSchedulesInfo.hasConnectionWindow = hasCommunicationWindow(comTaskExecution);
        setConnectionTaskInfo(comTaskExecution, deviceSchedulesInfo);
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
        deviceSchedulesInfo.comTask = ComTaskInfo.from(comTaskExecution.getComTask());
        deviceSchedulesInfo.version = comTaskExecution.getVersion();
        deviceSchedulesInfo.active = !comTaskExecution.isOnHold();
        deviceSchedulesInfo.hasConnectionWindow = hasCommunicationWindow(comTaskExecution);
        setConnectionTaskInfo(comTaskExecution, deviceSchedulesInfo);
        Device device = comTaskExecution.getDevice();
        deviceSchedulesInfo.parent = new VersionInfo<>(device.getName(), device.getVersion());
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

    private static void setConnectionTaskInfo(ComTaskExecution comTaskExecution, DeviceSchedulesInfo info) {
        if(comTaskExecution.getConnectionTask().isPresent()) {
            info.connectionDefinedOnDevice = true;
            if(comTaskExecution.getConnectionTask().get() instanceof ScheduledConnectionTask) {
                ScheduledConnectionTask task = (ScheduledConnectionTask) comTaskExecution.getConnectionTask().get();
                info.connectionStrategyKey = task.getConnectionStrategy().name();
            }
        }
    }


    private enum ScheduleType {
        ONREQUEST,SCHEDULED,INDIVIDUAL,ADHOC;
    }

}
