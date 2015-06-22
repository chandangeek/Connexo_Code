package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.CollectionUtil;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.configuration.rest.ConnectionStrategyAdapter;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.CompletionCodeInfo;
import com.energyict.mdc.device.data.rest.TaskStatusAdapter;
import com.energyict.mdc.device.data.tasks.*;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.scheduling.rest.ComTaskInfo;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeviceComTaskInfoFactory {
    private final Thesaurus thesaurus;
    private final TopologyService topologyService;
    private final TaskStatusAdapter taskStatusAdapter = new TaskStatusAdapter();

    @Inject
    public DeviceComTaskInfoFactory(Thesaurus thesaurus, TopologyService topologyService) {
        this.thesaurus = thesaurus;
        this.topologyService = topologyService;
    }

    public List<DeviceComTaskInfo> from(List<ComTaskExecution> comTaskExecutions, List<ComTaskEnablement> comTaskEnablements, Device device) {
        return comTaskEnablements
                    .stream()
                    .map(comTaskEnablement -> this.fromAllComTaskExecutions(comTaskEnablement, comTaskExecutions, device))
                    .collect(Collectors.toList());
    }

    private DeviceComTaskInfo fromAllComTaskExecutions(ComTaskEnablement comTaskEnablement, List<ComTaskExecution> comTaskExecutions, Device device) {
        List<ComTaskExecution> compatibleComTaskExecutions = comTaskExecutions.stream()
                .filter(comTaskExecution -> CollectionUtil.contains(comTaskExecution.getComTasks(), comTaskEnablement.getComTask()))
                .collect(Collectors.toList());
        if (!compatibleComTaskExecutions.isEmpty()) {
            return this.fromCompatibleComTaskExecutions(comTaskEnablement, compatibleComTaskExecutions);
        } else {
            return this.from(comTaskEnablement,device);
        }
    }

    private DeviceComTaskInfo fromCompatibleComTaskExecutions(ComTaskEnablement comTaskEnablement, List<ComTaskExecution> compatibleComTaskExecutions) {
        DeviceComTaskInfo deviceComTasksInfo = new DeviceComTaskInfo();
        deviceComTasksInfo.comTask = ComTaskInfo.from(comTaskEnablement.getComTask());
        deviceComTasksInfo.securitySettings = comTaskEnablement.getSecurityPropertySet().getName();
            for(ComTaskExecution comTaskExecution:compatibleComTaskExecutions){
                if(comTaskExecution.usesSharedSchedule()){
                    setFieldsForSharedScheduleExecution(deviceComTasksInfo, comTaskExecution);
                } else if(comTaskExecution.isScheduledManually() && !comTaskExecution.isAdHoc()){
                    setFieldsForIndividualScheduleExecution(deviceComTasksInfo, comTaskExecution);
                } else if(comTaskExecution.isAdHoc()){
                    setFieldsForIndividualScheduleExecution(deviceComTasksInfo, comTaskExecution);
                    deviceComTasksInfo.scheduleType = thesaurus.getString("onRequest","On request");
                }
            }


        return deviceComTasksInfo;
    }

    private void setFieldsForIndividualScheduleExecution(DeviceComTaskInfo deviceComTasksInfo, ComTaskExecution comTaskExecution) {
        deviceComTasksInfo.scheduleTypeKey = ScheduleTypeKey.INDIVIDUAL.name();
        deviceComTasksInfo.scheduleType = thesaurus.getString("individualSchedule","Individual schedule");
        deviceComTasksInfo.protocolDialect = ((SingleComTaskComTaskExecution)comTaskExecution).getProtocolDialectConfigurationProperties().getDeviceProtocolDialect().getDisplayName();
        if(comTaskExecution.getNextExecutionSpecs().isPresent()){
            deviceComTasksInfo.temporalExpression = TemporalExpressionInfo.from(comTaskExecution.getNextExecutionSpecs().get().getTemporalExpression());
        }
        deviceComTasksInfo.lastCommunicationStart = comTaskExecution.getLastExecutionStartTimestamp();
        deviceComTasksInfo.latestResult = comTaskExecution.getLastSession().map(ctes -> CompletionCodeInfo.from(ctes.getHighestPriorityCompletionCode(), thesaurus)).orElse(null);
        deviceComTasksInfo.successfulFinishTime = comTaskExecution.getLastSuccessfulCompletionTimestamp();
        deviceComTasksInfo.isOnHold = comTaskExecution.isOnHold();
        deviceComTasksInfo.status = thesaurus.getString(taskStatusAdapter.marshal(comTaskExecution.getStatus()),taskStatusAdapter.marshal(comTaskExecution.getStatus()));
        if (comTaskExecution.usesDefaultConnectionTask()) {
            if(comTaskExecution.getConnectionTask()!=null){
                deviceComTasksInfo.connectionMethod = thesaurus.getString(MessageSeeds.DEFAULT.getKey(),MessageSeeds.DEFAULT.getKey()) +
                        " (" + comTaskExecution.getConnectionTask().getName() + ")";
                deviceComTasksInfo.connectionDefinedOnDevice = true;
            } else {
              //  deviceComTasksInfo.connectionMethod = thesaurus.getString(MessageSeeds.DEFAULT_NOT_DEFINED.getKey(),MessageSeeds.DEFAULT_NOT_DEFINED.getKey());
                deviceComTasksInfo.connectionMethod = thesaurus.getString(MessageSeeds.DEFAULT.getKey(),MessageSeeds.DEFAULT.getKey());
                deviceComTasksInfo.connectionDefinedOnDevice = false;
            }
        }
        else {
            deviceComTasksInfo.connectionMethod = comTaskExecution.getConnectionTask().getName();
            deviceComTasksInfo.connectionDefinedOnDevice = true;
        }
        setConnectionStrategy(deviceComTasksInfo, comTaskExecution);
        deviceComTasksInfo.urgency = comTaskExecution.getPlannedPriority();
        deviceComTasksInfo.nextCommunication = comTaskExecution.getNextExecutionTimestamp();
        deviceComTasksInfo.plannedDate = comTaskExecution.getPlannedNextExecutionTimestamp();
    }

    private void setConnectionStrategy(DeviceComTaskInfo deviceComTasksInfo, ComTaskExecution comTaskExecution) {
        if(comTaskExecution.getConnectionTask()!= null && comTaskExecution.getConnectionTask() instanceof ScheduledConnectionTask){
            ConnectionStrategy connectionStrategy = ((ScheduledConnectionTask) comTaskExecution.getConnectionTask()).getConnectionStrategy();
            ConnectionStrategyAdapter connectionStrategyAdapter = new ConnectionStrategyAdapter();
            String connectionStrategyValue = connectionStrategyAdapter.marshal(connectionStrategy);
            deviceComTasksInfo.connectionStrategy = thesaurus.getString(connectionStrategyValue,connectionStrategyValue);
            deviceComTasksInfo.connectionStrategyKey = connectionStrategyValue;
        }
    }

    private void setFieldsForSharedScheduleExecution(DeviceComTaskInfo deviceComTasksInfo, ComTaskExecution comTaskExecution) {
        deviceComTasksInfo.temporalExpression = TemporalExpressionInfo.from(((ScheduledComTaskExecution) comTaskExecution).getComSchedule().getTemporalExpression());
        deviceComTasksInfo.scheduleName = ((ScheduledComTaskExecution) comTaskExecution).getComSchedule().getName();
        deviceComTasksInfo.scheduleTypeKey = ScheduleTypeKey.SHARED.name();
        deviceComTasksInfo.scheduleType = thesaurus.getString("masterSchedule","Shared schedule");
        deviceComTasksInfo.lastCommunicationStart = comTaskExecution.getLastExecutionStartTimestamp();
        deviceComTasksInfo.latestResult = comTaskExecution.getLastSession().map(ctes -> CompletionCodeInfo.from(ctes.getHighestPriorityCompletionCode(), thesaurus)).orElse(null);
        deviceComTasksInfo.successfulFinishTime = comTaskExecution.getLastSuccessfulCompletionTimestamp();
        deviceComTasksInfo.isOnHold = comTaskExecution.isOnHold();
        deviceComTasksInfo.status = thesaurus.getString(taskStatusAdapter.marshal(comTaskExecution.getStatus()),taskStatusAdapter.marshal(comTaskExecution.getStatus()));
        if (comTaskExecution.usesDefaultConnectionTask()) {
            if(comTaskExecution.getConnectionTask()!=null){
                deviceComTasksInfo.connectionMethod = thesaurus.getString(MessageSeeds.DEFAULT.getKey(),MessageSeeds.DEFAULT.getKey()) +
                        " (" + comTaskExecution.getConnectionTask().getName() + ")";
                deviceComTasksInfo.connectionDefinedOnDevice = true;
            } else {
               // deviceComTasksInfo.connectionMethod = thesaurus.getString(MessageSeeds.DEFAULT_NOT_DEFINED.getKey(),MessageSeeds.DEFAULT_NOT_DEFINED.getKey());
                deviceComTasksInfo.connectionMethod = thesaurus.getString(MessageSeeds.DEFAULT.getKey(),MessageSeeds.DEFAULT.getKey());
                deviceComTasksInfo.connectionDefinedOnDevice = false;
            }
            setConnectionStrategy(deviceComTasksInfo,comTaskExecution);
        }
        else {
            ConnectionTask<?, ?> connectionTask = comTaskExecution.getConnectionTask();
            deviceComTasksInfo.connectionMethod = connectionTask.getName();
            deviceComTasksInfo.connectionDefinedOnDevice = true;
            ConnectionStrategyAdapter connectionStrategyAdapter = new ConnectionStrategyAdapter();
            if (connectionTask instanceof ScheduledConnectionTask) {
                ScheduledConnectionTask scheduledConnectionTask = (ScheduledConnectionTask) connectionTask;
                ConnectionStrategy connectionStrategy = scheduledConnectionTask.getConnectionStrategy();
                String connectionStrategyValue = connectionStrategyAdapter.marshal(connectionStrategy);
                deviceComTasksInfo.connectionStrategy = thesaurus.getString(connectionStrategyValue,connectionStrategyValue);
                deviceComTasksInfo.connectionStrategyKey = connectionStrategyValue;
            }
            else {
                deviceComTasksInfo.connectionStrategy = thesaurus.getString(MessageSeeds.CONNECTION_TYPE_STRATEGY_NOT_APPLICABLE.name(), null);
            }
        }
        deviceComTasksInfo.urgency = comTaskExecution.getPlannedPriority();
        deviceComTasksInfo.nextCommunication = comTaskExecution.getNextExecutionTimestamp();
        deviceComTasksInfo.plannedDate = comTaskExecution.getPlannedNextExecutionTimestamp();
    }

    private DeviceComTaskInfo from(ComTaskEnablement comTaskEnablement, Device device) {
        DeviceComTaskInfo deviceComTasksInfo = new DeviceComTaskInfo();
        deviceComTasksInfo.scheduleType = thesaurus.getString("onRequest","On request");
        deviceComTasksInfo.scheduleTypeKey = ScheduleTypeKey.ON_REQUEST.name();
        deviceComTasksInfo.comTask = ComTaskInfo.from(comTaskEnablement.getComTask());
        deviceComTasksInfo.status = thesaurus.getString(taskStatusAdapter.marshal(TaskStatus.OnHold),taskStatusAdapter.marshal(TaskStatus.OnHold));
        if(comTaskEnablement.usesDefaultConnectionTask()){
            if(comTaskEnablement.getPartialConnectionTask().isPresent()){
                PartialConnectionTask partialConnectionTask = comTaskEnablement.getPartialConnectionTask().get();

                Optional<ConnectionTask> deviceConnectionTaskOptional = findDefaultConnectionTaskInCompleteTopology(device);
                if (deviceConnectionTaskOptional.isPresent()) {
                    deviceComTasksInfo.connectionMethod = thesaurus.getString(MessageSeeds.DEFAULT.getKey(),MessageSeeds.DEFAULT.getKey()) +
                            " (" + deviceConnectionTaskOptional.get().getName() + ")";
                    deviceComTasksInfo.connectionDefinedOnDevice = true;
                } else {
                    deviceComTasksInfo.connectionMethod = thesaurus.getString(MessageSeeds.DEFAULT.getKey(),MessageSeeds.DEFAULT.getKey()) +
                            " (" + partialConnectionTask.getName() + ")";
                           // + thesaurus.getString(MessageSeeds.NOT_DEFINED_YET.getKey(),MessageSeeds.NOT_DEFINED_YET.getKey());
                    deviceComTasksInfo.connectionDefinedOnDevice = false;
                }

                if(partialConnectionTask instanceof PartialScheduledConnectionTask){
                    ConnectionStrategy connectionStrategy = ((PartialScheduledConnectionTask) partialConnectionTask).getConnectionStrategy();
                    ConnectionStrategyAdapter connectionStrategyAdapter = new ConnectionStrategyAdapter();
                    String connectionStrategyValue = connectionStrategyAdapter.marshal(connectionStrategy);
                    deviceComTasksInfo.connectionStrategy = thesaurus.getString(connectionStrategyValue,connectionStrategyValue);
                    deviceComTasksInfo.connectionStrategyKey = connectionStrategyValue;
                }

            } else {
                Optional<ConnectionTask> deviceConnectionTaskOptional = findDefaultConnectionTaskInCompleteTopology(device);
                if(deviceConnectionTaskOptional.isPresent()){
                    deviceComTasksInfo.connectionMethod = thesaurus.getString(MessageSeeds.DEFAULT.getKey(),MessageSeeds.DEFAULT.getKey()) +
                            " (" + deviceConnectionTaskOptional.get().getName() + ")";
                    deviceComTasksInfo.connectionDefinedOnDevice = true;
                } else {
               //     deviceComTasksInfo.connectionMethod = thesaurus.getString(MessageSeeds.DEFAULT_NOT_DEFINED.getKey(),MessageSeeds.DEFAULT_NOT_DEFINED.getKey());
                    deviceComTasksInfo.connectionMethod = thesaurus.getString(MessageSeeds.DEFAULT.getKey(),MessageSeeds.DEFAULT.getKey());
                    deviceComTasksInfo.connectionDefinedOnDevice = false;
                }


            }
        } else {
            if (comTaskEnablement.getPartialConnectionTask().isPresent()) {
                PartialConnectionTask partialConnectionTask = comTaskEnablement.getPartialConnectionTask().get();
                Optional<ConnectionTask<?, ?>> deviceConnectionTaskOptional = device.getConnectionTasks().stream().filter(connectionTask -> connectionTask.getName().equals(partialConnectionTask.getName())).findFirst();
                if(deviceConnectionTaskOptional.isPresent()){
                    deviceComTasksInfo.connectionMethod = partialConnectionTask.getName();
                    deviceComTasksInfo.connectionDefinedOnDevice = true;
                } else {
                    deviceComTasksInfo.connectionMethod = partialConnectionTask.getName(); // + ' ' + thesaurus.getString(MessageSeeds.NOT_DEFINED_YET.getKey(),MessageSeeds.NOT_DEFINED_YET.getKey());
                    deviceComTasksInfo.connectionDefinedOnDevice = false;
                }

                if(partialConnectionTask instanceof PartialScheduledConnectionTask){
                    ConnectionStrategy connectionStrategy = ((PartialScheduledConnectionTask) partialConnectionTask).getConnectionStrategy();
                    ConnectionStrategyAdapter connectionStrategyAdapter = new ConnectionStrategyAdapter();
                    String connectionStrategyValue = connectionStrategyAdapter.marshal(connectionStrategy);
                    deviceComTasksInfo.connectionStrategy = thesaurus.getString(connectionStrategyValue,connectionStrategyValue);
                    deviceComTasksInfo.connectionStrategyKey = connectionStrategyValue;
                }
            }
        }

        deviceComTasksInfo.urgency = comTaskEnablement.getPriority();
        deviceComTasksInfo.securitySettings = comTaskEnablement.getSecurityPropertySet().getName();
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = comTaskEnablement.getProtocolDialectConfigurationProperties();
        deviceComTasksInfo.protocolDialect = protocolDialectConfigurationProperties.getDeviceProtocolDialect().getDisplayName();
        return deviceComTasksInfo;
    }

    private Optional<ConnectionTask> findDefaultConnectionTaskInCompleteTopology(Device device) {
        return this.topologyService.findDefaultConnectionTaskForTopology(device);
    }

    private enum ScheduleTypeKey{
        ON_REQUEST,
        INDIVIDUAL,
        SHARED
    }
}
