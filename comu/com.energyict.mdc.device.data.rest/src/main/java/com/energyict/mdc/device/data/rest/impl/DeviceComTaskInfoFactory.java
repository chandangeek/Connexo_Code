/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.ConnectionStrategy;
import com.energyict.mdc.common.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.protocol.ConnectionFunction;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.common.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.common.tasks.history.CompletionCode;
import com.energyict.mdc.device.data.rest.CompletionCodeInfo;
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
                .filter(comTaskExecution -> comTaskExecution.getComTask().equals(comTaskEnablement.getComTask()))
                .collect(Collectors.toList());
        if (!compatibleComTaskExecutions.isEmpty()) {
            return this.fromCompatibleComTaskExecutions(comTaskEnablement, compatibleComTaskExecutions);
        } else {
            return this.setFieldsForEnablementWhichIsNotOnDeviceYet(comTaskEnablement, device);
        }
    }

    private DeviceComTaskInfo fromCompatibleComTaskExecutions(ComTaskEnablement comTaskEnablement, List<ComTaskExecution> compatibleComTaskExecutions) {
        DeviceComTaskInfo deviceComTaskInfo = new DeviceComTaskInfo();
        deviceComTaskInfo.comTask = ComTaskInfo.from(comTaskEnablement.getComTask());
        deviceComTaskInfo.securitySettings = comTaskEnablement.getSecurityPropertySet().getName();
        for (ComTaskExecution comTaskExecution : compatibleComTaskExecutions) {
            if (comTaskExecution.usesSharedSchedule()) {
                setFieldsForSharedScheduleExecution(deviceComTaskInfo, comTaskExecution, comTaskEnablement);
            } else if (comTaskExecution.isScheduledManually() && !comTaskExecution.isAdHoc()) {
                setFieldsForIndividualScheduleExecution(deviceComTaskInfo, comTaskExecution, comTaskEnablement);
            } else if (comTaskExecution.isAdHoc()) {
                setFieldsForIndividualScheduleExecution(deviceComTaskInfo, comTaskExecution, comTaskEnablement);
                deviceComTaskInfo.scheduleType = thesaurus.getFormat(DefaultTranslationKey.ON_REQUEST).format();
            }
        }

        return deviceComTaskInfo;
    }

    private void setFieldsForIndividualScheduleExecution(DeviceComTaskInfo deviceComTasksInfo, ComTaskExecution comTaskExecution, ComTaskEnablement comTaskEnablement) {
        deviceComTasksInfo.scheduleTypeKey = ScheduleTypeKey.INDIVIDUAL.name();
        deviceComTasksInfo.scheduleType = thesaurus.getFormat(DefaultTranslationKey.INDIVIDUAL_SCHEDULE).format();
        if (comTaskExecution.getNextExecutionSpecs().isPresent()) {
            deviceComTasksInfo.temporalExpression = TemporalExpressionInfo.from(comTaskExecution.getNextExecutionSpecs().get().getTemporalExpression());
        }
        setFieldsForScheduledExecution(deviceComTasksInfo, comTaskExecution, comTaskEnablement);
    }

    private void setFieldsForScheduledExecution(DeviceComTaskInfo deviceComTasksInfo, ComTaskExecution comTaskExecution, ComTaskEnablement comTaskEnablement) {
        deviceComTasksInfo.lastCommunicationStart = comTaskExecution.getLastExecutionStartTimestamp();
        deviceComTasksInfo.latestResult =
                comTaskExecution
                        .getLastSession()
                        .map(ComTaskExecutionSession::getHighestPriorityCompletionCode)
                        .map(this::infoFrom)
                        .orElse(null);
        deviceComTasksInfo.successfulFinishTime = comTaskExecution.getLastSuccessfulCompletionTimestamp();
        deviceComTasksInfo.isOnHold = comTaskExecution.isOnHold();
        deviceComTasksInfo.status = TaskStatusTranslationKeys.translationFor(comTaskExecution.getStatus(), thesaurus);
        deviceComTasksInfo.urgency = comTaskExecution.getPlannedPriority();
        deviceComTasksInfo.ignoreNextExecutionSpecsForInbound = comTaskExecution.isIgnoreNextExecutionSpecsForInbound();
        deviceComTasksInfo.nextCommunication = comTaskExecution.getNextExecutionTimestamp();
        deviceComTasksInfo.plannedDate = comTaskExecution.getPlannedNextExecutionTimestamp();
        deviceComTasksInfo.connectionFunctionInfo = comTaskExecution.getConnectionFunction().isPresent() ? new ConnectionFunctionInfo(comTaskExecution.getConnectionFunction().get()) : null;
        deviceComTasksInfo.maxNumberOfTries = comTaskExecution.getMaxNumberOfTries();
        setConnectionMethodInfo(comTaskExecution, comTaskEnablement, deviceComTasksInfo);
        setConnectionStrategy(deviceComTasksInfo, comTaskExecution, comTaskEnablement);
    }

    private CompletionCodeInfo infoFrom(CompletionCode completionCode) {
        return new CompletionCodeInfo(completionCode.name(), CompletionCodeTranslationKeys.translationFor(completionCode, thesaurus));
    }

    private void setFieldsForSharedScheduleExecution(DeviceComTaskInfo deviceComTasksInfo, ComTaskExecution comTaskExecution, ComTaskEnablement comTaskEnablement) {
        deviceComTasksInfo.temporalExpression = TemporalExpressionInfo.from(comTaskExecution.getComSchedule().get().getTemporalExpression());
        deviceComTasksInfo.scheduleName = comTaskExecution.getComSchedule().get().getName();
        deviceComTasksInfo.scheduleTypeKey = ScheduleTypeKey.SHARED.name();
        deviceComTasksInfo.scheduleType = thesaurus.getFormat(DefaultTranslationKey.SHARED_SCHEDULE).format();
        setFieldsForScheduledExecution(deviceComTasksInfo, comTaskExecution, comTaskEnablement);
    }

    private DeviceComTaskInfo setFieldsForEnablementWhichIsNotOnDeviceYet(ComTaskEnablement comTaskEnablement, Device device) {
        DeviceComTaskInfo deviceComTasksInfo = new DeviceComTaskInfo();
        deviceComTasksInfo.scheduleType = thesaurus.getFormat(DefaultTranslationKey.ON_REQUEST).format();
        deviceComTasksInfo.scheduleTypeKey = ScheduleTypeKey.ON_REQUEST.name();
        deviceComTasksInfo.comTask = ComTaskInfo.from(comTaskEnablement.getComTask());
        deviceComTasksInfo.status = thesaurus.getFormat(TaskStatusTranslationKeys.WAITING).format();
        deviceComTasksInfo.urgency = comTaskEnablement.getPriority();
        deviceComTasksInfo.securitySettings = comTaskEnablement.getSecurityPropertySet().getName();
        deviceComTasksInfo.ignoreNextExecutionSpecsForInbound = comTaskEnablement.isIgnoreNextExecutionSpecsForInbound();
        deviceComTasksInfo.connectionFunctionInfo = comTaskEnablement.getConnectionFunction().isPresent() ? new ConnectionFunctionInfo(comTaskEnablement.getConnectionFunction().get()) : null;
        deviceComTasksInfo.maxNumberOfTries = comTaskEnablement.getMaxNumberOfTries();
        setConnectionMethodInfo(comTaskEnablement, device, deviceComTasksInfo);
        setConnectionTaskStrategy(deviceComTasksInfo, comTaskEnablement);
        return deviceComTasksInfo;
    }

    private void setConnectionStrategy(DeviceComTaskInfo deviceComTasksInfo, ComTaskExecution comTaskExecution, ComTaskEnablement comTaskEnablement) {
        if (comTaskExecution.getConnectionTask().isPresent() && comTaskExecution.getConnectionTask().get() instanceof ScheduledConnectionTask) {
            ConnectionStrategy connectionStrategy = ((ScheduledConnectionTask) comTaskExecution.getConnectionTask().get()).getConnectionStrategy();
            deviceComTasksInfo.connectionStrategy = ConnectionStrategyTranslationKeys.translationFor(connectionStrategy, thesaurus);
            deviceComTasksInfo.connectionStrategyKey = connectionStrategy.name();
        } else {
            setConnectionTaskStrategy(deviceComTasksInfo, comTaskEnablement);
        }
    }

    private void setConnectionTaskStrategy(DeviceComTaskInfo deviceComTasksInfo, ComTaskEnablement comTaskEnablement) {
        if (comTaskEnablement.getPartialConnectionTask().isPresent() && comTaskEnablement.getPartialConnectionTask().get() instanceof PartialScheduledConnectionTask) {
            ConnectionStrategy connectionStrategy = ((PartialScheduledConnectionTask) comTaskEnablement.getPartialConnectionTask().get()).getConnectionStrategy();
            deviceComTasksInfo.connectionStrategy = ConnectionStrategyTranslationKeys.translationFor(connectionStrategy, thesaurus);
            deviceComTasksInfo.connectionStrategyKey = connectionStrategy.name();
        }
    }

    private void setConnectionMethodInfo(ComTaskExecution comTaskExecution, ComTaskEnablement comTaskEnablement, DeviceComTaskInfo deviceComTasksInfo) {
        deviceComTasksInfo.connectionDefinedOnDevice = comTaskExecution.getConnectionTask().isPresent();
        if (comTaskExecution.usesDefaultConnectionTask()) {
            deviceComTasksInfo.connectionMethod = comTaskExecution.getConnectionTask().isPresent()
                    ? thesaurus.getFormat(DefaultTranslationKey.DEFAULT).format() + " (" + comTaskExecution.getConnectionTask().get().getName() + ")"
                    : thesaurus.getFormat(DefaultTranslationKey.DEFAULT).format();
        } else if (comTaskExecution.getConnectionFunction().isPresent()) {
            ConnectionFunction connectionFunction = comTaskExecution.getConnectionFunction().get();
            String connectionFunctionDisplayName = connectionFunction.getConnectionFunctionDisplayName();
            deviceComTasksInfo.connectionMethod = comTaskExecution.getConnectionTask().isPresent()
                    ? thesaurus.getFormat(DefaultTranslationKey.CONNECTION_FUNCTION).format(connectionFunctionDisplayName) + " (" + comTaskExecution.getConnectionTask().get().getName() + ")"
                    : thesaurus.getFormat(DefaultTranslationKey.CONNECTION_FUNCTION).format(connectionFunctionDisplayName);
        } else {
            deviceComTasksInfo.connectionMethod = comTaskExecution.getConnectionTask().isPresent()
                    ? comTaskExecution.getConnectionTask().get().getName()
                    : comTaskEnablement.getPartialConnectionTask().isPresent() ? comTaskEnablement.getPartialConnectionTask().get().getName() : null;
        }
    }

    private void setConnectionMethodInfo(ComTaskEnablement comTaskEnablement, Device device, DeviceComTaskInfo deviceComTasksInfo) {
        if (comTaskEnablement.usesDefaultConnectionTask()) {
            Optional<ConnectionTask> deviceConnectionTaskOptional = findDefaultConnectionTaskInCompleteTopology(device);
            deviceComTasksInfo.connectionDefinedOnDevice = deviceConnectionTaskOptional.isPresent();
            deviceComTasksInfo.connectionMethod = deviceConnectionTaskOptional.isPresent()
                    ? thesaurus.getFormat(DefaultTranslationKey.DEFAULT).format() + " (" + deviceConnectionTaskOptional.get().getName() + ")"
                    : thesaurus.getFormat(DefaultTranslationKey.DEFAULT).format();
        } else if (comTaskEnablement.getConnectionFunction().isPresent()) {
            ConnectionFunction connectionFunction = comTaskEnablement.getConnectionFunction().get();
            String connectionFunctionDisplayName = connectionFunction.getConnectionFunctionDisplayName();
            Optional<ConnectionTask> deviceConnectionTaskOptional = findConnectionTaskWithConnectionFunctionInCompleteTopology(device, connectionFunction);
            deviceComTasksInfo.connectionDefinedOnDevice = deviceConnectionTaskOptional.isPresent();
            deviceComTasksInfo.connectionMethod = deviceConnectionTaskOptional.isPresent()
                    ? thesaurus.getFormat(DefaultTranslationKey.CONNECTION_FUNCTION).format(connectionFunctionDisplayName) + " (" + deviceConnectionTaskOptional.get().getName() + ")"
                    : thesaurus.getFormat(DefaultTranslationKey.CONNECTION_FUNCTION).format(connectionFunctionDisplayName);
        } else if (comTaskEnablement.getPartialConnectionTask().isPresent()) {
            PartialConnectionTask partialConnectionTask = comTaskEnablement.getPartialConnectionTask().get();
            Optional<ConnectionTask> deviceConnectionTaskOptional = device.getConnectionTasks().stream()
                    .filter(connectionTask -> connectionTask.getName().equals(partialConnectionTask.getName()))
                    .map(connectionTask -> (ConnectionTask) connectionTask)
                    .findFirst();
            deviceComTasksInfo.connectionDefinedOnDevice = deviceConnectionTaskOptional.isPresent();
            deviceComTasksInfo.connectionMethod = partialConnectionTask.getName();
        }
    }

    private Optional<ConnectionTask> findDefaultConnectionTaskInCompleteTopology(Device device) {
        return this.topologyService.findDefaultConnectionTaskForTopology(device);
    }

    private Optional<ConnectionTask> findConnectionTaskWithConnectionFunctionInCompleteTopology(Device device, ConnectionFunction connectionFunction) {
        return this.topologyService.findConnectionTaskWithConnectionFunctionForTopology(device, connectionFunction);
    }

    private enum ScheduleTypeKey {
        ON_REQUEST,
        INDIVIDUAL,
        SHARED
    }
}