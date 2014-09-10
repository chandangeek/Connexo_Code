package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.CollectionUtil;
import com.energyict.mdc.device.config.*;
import com.energyict.mdc.device.configuration.rest.ConnectionStrategyAdapter;
import com.energyict.mdc.device.data.rest.TaskStatusAdapter;
import com.energyict.mdc.device.data.tasks.*;
import com.energyict.mdc.scheduling.rest.ComTaskInfo;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;


public class DeviceComTaskInfoFactory {
    private final Thesaurus thesaurus;
    private final TaskStatusAdapter taskStatusAdapter = new TaskStatusAdapter();

    @Inject
    public DeviceComTaskInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public List<DeviceComTasksInfo> from(List<ComTaskExecution> comTaskExecutions, List<ComTaskEnablement> comTaskEnablements) {
        List<DeviceComTasksInfo> results = new ArrayList<>();
        for (ComTaskEnablement comTaskEnablement : comTaskEnablements) {
            results.add(this.fromAllComTaskExecutions(comTaskEnablement, comTaskExecutions));
        }
        return results;
    }

    private DeviceComTasksInfo fromAllComTaskExecutions(ComTaskEnablement comTaskEnablement, List<ComTaskExecution> comTaskExecutions) {
        List<ComTaskExecution> compatibleComTaskExecutions = new ArrayList<>();
        for(ComTaskExecution comTaskExecution:comTaskExecutions){
            if(CollectionUtil.contains(comTaskExecution.getComTasks(), comTaskEnablement.getComTask())){
                compatibleComTaskExecutions.add(comTaskExecution);
            }
        }
        if (!compatibleComTaskExecutions.isEmpty()) {
            return this.fromCompatibleComTaskExecutions(comTaskEnablement, compatibleComTaskExecutions);
        } else {
            return this.from(comTaskEnablement);
        }
    }

    private DeviceComTasksInfo fromCompatibleComTaskExecutions(ComTaskEnablement comTaskEnablement, List<ComTaskExecution> compatibleComTaskExecutions) {
        DeviceComTasksInfo deviceComTasksInfo = new DeviceComTasksInfo();
        deviceComTasksInfo.comTask = ComTaskInfo.from(comTaskEnablement.getComTask());
        deviceComTasksInfo.securitySettings = comTaskEnablement.getSecurityPropertySet().getName();
        deviceComTasksInfo.protocolDialect = comTaskEnablement.getProtocolDialectConfigurationProperties().get().getName();
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

    private void setFieldsForIndividualScheduleExecution(DeviceComTasksInfo deviceComTasksInfo, ComTaskExecution comTaskExecution) {
        deviceComTasksInfo.scheduleType = thesaurus.getString("individualSchedule","Individual schedule");
        deviceComTasksInfo.temporalExpression = TemporalExpressionInfo.from(comTaskExecution.getNextExecutionSpecs().get().getTemporalExpression());
        deviceComTasksInfo.lastCommunicationStart = comTaskExecution.getLastExecutionStartTimestamp();
        deviceComTasksInfo.status = thesaurus.getString(taskStatusAdapter.marshal(comTaskExecution.getStatus()),taskStatusAdapter.marshal(comTaskExecution.getStatus()));
        if (comTaskExecution.useDefaultConnectionTask()) {
            deviceComTasksInfo.connectionMethod = thesaurus.getString("default", "Default");
        }
        else {
            deviceComTasksInfo.connectionMethod = comTaskExecution.getConnectionTask().getName();
        }
        setConnectionStrategy(deviceComTasksInfo, comTaskExecution);
        deviceComTasksInfo.urgency = comTaskExecution.getPlannedPriority();
        deviceComTasksInfo.nextCommunication = comTaskExecution.getNextExecutionTimestamp();
        deviceComTasksInfo.plannedDate = comTaskExecution.getPlannedNextExecutionTimestamp();
    }

    private void setConnectionStrategy(DeviceComTasksInfo deviceComTasksInfo, ComTaskExecution comTaskExecution) {
        if(comTaskExecution.getConnectionTask()!= null && comTaskExecution.getConnectionTask() instanceof ScheduledConnectionTask){
            ConnectionStrategy connectionStrategy = ((ScheduledConnectionTask) comTaskExecution.getConnectionTask()).getConnectionStrategy();
            ConnectionStrategyAdapter connectionStrategyAdapter = new ConnectionStrategyAdapter();
            String connectionStrategyValue = connectionStrategyAdapter.marshal(connectionStrategy);
            deviceComTasksInfo.connectionStrategy = thesaurus.getString(connectionStrategyValue,connectionStrategyValue);
        }
    }

    private void setFieldsForSharedScheduleExecution(DeviceComTasksInfo deviceComTasksInfo, ComTaskExecution comTaskExecution) {
        deviceComTasksInfo.temporalExpression = TemporalExpressionInfo.from(((ScheduledComTaskExecution) comTaskExecution).getComSchedule().getTemporalExpression());
        deviceComTasksInfo.scheduleName = ((ScheduledComTaskExecution) comTaskExecution).getComSchedule().getName();
        deviceComTasksInfo.scheduleType = thesaurus.getString("masterSchedule","Master schedule");
        deviceComTasksInfo.lastCommunicationStart = comTaskExecution.getLastExecutionStartTimestamp();
        deviceComTasksInfo.status = thesaurus.getString(taskStatusAdapter.marshal(comTaskExecution.getStatus()),taskStatusAdapter.marshal(comTaskExecution.getStatus()));
        if (comTaskExecution.useDefaultConnectionTask()) {
            deviceComTasksInfo.connectionMethod = thesaurus.getString("default", "Default");
            setConnectionStrategy(deviceComTasksInfo,comTaskExecution);
        }
        else {
            deviceComTasksInfo.connectionMethod = comTaskExecution.getConnectionTask().getName();
            ConnectionStrategy connectionStrategy = ((ScheduledComTaskExecution)comTaskExecution).getConnectionTask().getConnectionStrategy();
            ConnectionStrategyAdapter connectionStrategyAdapter = new ConnectionStrategyAdapter();
            String connectionStrategyValue = connectionStrategyAdapter.marshal(connectionStrategy);
            deviceComTasksInfo.connectionStrategy = thesaurus.getString(connectionStrategyValue,connectionStrategyValue);
        }
        deviceComTasksInfo.urgency = comTaskExecution.getPlannedPriority();
        deviceComTasksInfo.nextCommunication = comTaskExecution.getNextExecutionTimestamp();
        deviceComTasksInfo.plannedDate = comTaskExecution.getPlannedNextExecutionTimestamp();
    }

    private DeviceComTasksInfo from(ComTaskEnablement comTaskEnablement) {
        DeviceComTasksInfo deviceComTasksInfo = new DeviceComTasksInfo();
        deviceComTasksInfo.scheduleType = thesaurus.getString("onRequest","On request");
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
            deviceComTasksInfo.protocolDialect = protocolDialectConfigurationProperties.getDeviceProtocolDialectName();
        }
        return deviceComTasksInfo;
    }
}
