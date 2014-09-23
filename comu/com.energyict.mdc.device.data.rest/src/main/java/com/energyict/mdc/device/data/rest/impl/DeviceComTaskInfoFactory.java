package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.CollectionUtil;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.configuration.rest.ConnectionStrategyAdapter;
import com.energyict.mdc.device.data.rest.TaskStatusAdapter;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.scheduling.rest.ComTaskInfo;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DeviceComTaskInfoFactory {
    private final Thesaurus thesaurus;
    private final TaskStatusAdapter taskStatusAdapter = new TaskStatusAdapter();


    @Inject
    public DeviceComTaskInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public List<DeviceComTaskInfo> from(List<ComTaskExecution> comTaskExecutions, List<ComTaskEnablement> comTaskEnablements) {
        List<DeviceComTaskInfo> results = comTaskEnablements.stream()
                .map(comTaskEnablement -> this.fromAllComTaskExecutions(comTaskEnablement, comTaskExecutions))
                .collect(Collectors.toList());
        return results;
    }

    private DeviceComTaskInfo fromAllComTaskExecutions(ComTaskEnablement comTaskEnablement, List<ComTaskExecution> comTaskExecutions) {
        List<ComTaskExecution> compatibleComTaskExecutions = comTaskExecutions.stream()
                .filter(comTaskExecution -> CollectionUtil.contains(comTaskExecution.getComTasks(), comTaskEnablement.getComTask()))
                .collect(Collectors.toList());
        if (!compatibleComTaskExecutions.isEmpty()) {
            return this.fromCompatibleComTaskExecutions(comTaskEnablement, compatibleComTaskExecutions);
        } else {
            return this.from(comTaskEnablement);
        }
    }

    private DeviceComTaskInfo fromCompatibleComTaskExecutions(ComTaskEnablement comTaskEnablement, List<ComTaskExecution> compatibleComTaskExecutions) {
        DeviceComTaskInfo deviceComTasksInfo = new DeviceComTaskInfo();
        deviceComTasksInfo.comTask = ComTaskInfo.from(comTaskEnablement.getComTask());
        deviceComTasksInfo.securitySettings = comTaskEnablement.getSecurityPropertySet().getName();
        deviceComTasksInfo.protocolDialect = comTaskEnablement.getProtocolDialectConfigurationProperties().get().getDeviceProtocolDialect().getDisplayName();
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
        if(comTaskExecution.getNextExecutionSpecs().isPresent()){
            deviceComTasksInfo.temporalExpression = TemporalExpressionInfo.from(comTaskExecution.getNextExecutionSpecs().get().getTemporalExpression());
        }
        deviceComTasksInfo.lastCommunicationStart = comTaskExecution.getLastExecutionStartTimestamp();
        deviceComTasksInfo.status = thesaurus.getString(taskStatusAdapter.marshal(comTaskExecution.getStatus()),taskStatusAdapter.marshal(comTaskExecution.getStatus()));
        if (comTaskExecution.useDefaultConnectionTask()) {
            if(comTaskExecution.getConnectionTask()!=null){
                deviceComTasksInfo.connectionMethod = thesaurus.getString(MessageSeeds.DEFAULT.getKey(),MessageSeeds.DEFAULT.getKey()) +
                        " (" + comTaskExecution.getConnectionTask().getName() + ")";
            } else {
                deviceComTasksInfo.connectionMethod = thesaurus.getString(MessageSeeds.DEFAULT_NOT_DEFINED.getKey(),MessageSeeds.DEFAULT_NOT_DEFINED.getKey());
            }
        }
        else {
            deviceComTasksInfo.connectionMethod = comTaskExecution.getConnectionTask().getName();
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
        }
    }

    private void setFieldsForSharedScheduleExecution(DeviceComTaskInfo deviceComTasksInfo, ComTaskExecution comTaskExecution) {
        deviceComTasksInfo.temporalExpression = TemporalExpressionInfo.from(((ScheduledComTaskExecution) comTaskExecution).getComSchedule().getTemporalExpression());
        deviceComTasksInfo.scheduleName = ((ScheduledComTaskExecution) comTaskExecution).getComSchedule().getName();
        deviceComTasksInfo.scheduleTypeKey = ScheduleTypeKey.SHARED.name();
        deviceComTasksInfo.scheduleType = thesaurus.getString("masterSchedule","Master schedule");
        deviceComTasksInfo.lastCommunicationStart = comTaskExecution.getLastExecutionStartTimestamp();
        deviceComTasksInfo.status = thesaurus.getString(taskStatusAdapter.marshal(comTaskExecution.getStatus()),taskStatusAdapter.marshal(comTaskExecution.getStatus()));
        if (comTaskExecution.useDefaultConnectionTask()) {
            if(comTaskExecution.getConnectionTask()!=null){
                deviceComTasksInfo.connectionMethod = thesaurus.getString(MessageSeeds.DEFAULT.getKey(),MessageSeeds.DEFAULT.getKey()) +
                        " (" + comTaskExecution.getConnectionTask().getName() + ")";
            } else {
                deviceComTasksInfo.connectionMethod = thesaurus.getString(MessageSeeds.DEFAULT_NOT_DEFINED.getKey(),MessageSeeds.DEFAULT_NOT_DEFINED.getKey());
            }
            setConnectionStrategy(deviceComTasksInfo,comTaskExecution);
        }
        else {
            ConnectionTask<?, ?> connectionTask = comTaskExecution.getConnectionTask();
            deviceComTasksInfo.connectionMethod = connectionTask.getName();
            ConnectionStrategyAdapter connectionStrategyAdapter = new ConnectionStrategyAdapter();
            if (connectionTask instanceof ScheduledConnectionTask) {
                ScheduledConnectionTask scheduledConnectionTask = (ScheduledConnectionTask) connectionTask;
                ConnectionStrategy connectionStrategy = scheduledConnectionTask.getConnectionStrategy();
                String connectionStrategyValue = connectionStrategyAdapter.marshal(connectionStrategy);
                deviceComTasksInfo.connectionStrategy = thesaurus.getString(connectionStrategyValue,connectionStrategyValue);
            }
            else {
                deviceComTasksInfo.connectionStrategy = thesaurus.getString(MessageSeeds.CONNECTION_TYPE_STRATEGY_NOT_APPLICABLE.name(), null);
            }
        }
        deviceComTasksInfo.urgency = comTaskExecution.getPlannedPriority();
        deviceComTasksInfo.nextCommunication = comTaskExecution.getNextExecutionTimestamp();
        deviceComTasksInfo.plannedDate = comTaskExecution.getPlannedNextExecutionTimestamp();
    }

    private DeviceComTaskInfo from(ComTaskEnablement comTaskEnablement) {
        DeviceComTaskInfo deviceComTasksInfo = new DeviceComTaskInfo();
        deviceComTasksInfo.scheduleType = thesaurus.getString("onRequest","On request");
        deviceComTasksInfo.scheduleTypeKey = ScheduleTypeKey.ON_REQUEST.name();
        deviceComTasksInfo.comTask = ComTaskInfo.from(comTaskEnablement.getComTask());
        if(comTaskEnablement.getPartialConnectionTask().isPresent()){
            PartialConnectionTask partialConnectionTask = comTaskEnablement.getPartialConnectionTask().get();
            deviceComTasksInfo.connectionMethod = partialConnectionTask.getName();
            if(partialConnectionTask instanceof PartialScheduledConnectionTask){
                ConnectionStrategy connectionStrategy = ((PartialScheduledConnectionTask) partialConnectionTask).getConnectionStrategy();
                ConnectionStrategyAdapter connectionStrategyAdapter = new ConnectionStrategyAdapter();
                String connectionStrategyValue = connectionStrategyAdapter.marshal(connectionStrategy);
                deviceComTasksInfo.connectionStrategy = thesaurus.getString(connectionStrategyValue,connectionStrategyValue);
            }
        }
        deviceComTasksInfo.urgency = comTaskEnablement.getPriority();
        deviceComTasksInfo.securitySettings = comTaskEnablement.getSecurityPropertySet().getName();
        if(comTaskEnablement.getProtocolDialectConfigurationProperties().isPresent()){
            ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = comTaskEnablement.getProtocolDialectConfigurationProperties().get();
            deviceComTasksInfo.protocolDialect = protocolDialectConfigurationProperties.getDeviceProtocolDialect().getDisplayName();
        }
        return deviceComTasksInfo;
    }

    private enum ScheduleTypeKey{
        ON_REQUEST,
        INDIVIDUAL,
        SHARED
    }
}
