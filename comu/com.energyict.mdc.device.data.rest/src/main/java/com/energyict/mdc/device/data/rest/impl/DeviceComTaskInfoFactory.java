package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.CollectionUtil;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.CompletionCodeInfo;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.SingleComTaskComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.scheduling.rest.ComTaskInfo;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import com.elster.jupiter.nls.Thesaurus;

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
                if (comTaskExecution.usesSharedSchedule()) {
                    setFieldsForSharedScheduleExecution(deviceComTasksInfo, comTaskExecution);
                } else if (comTaskExecution.isScheduledManually() && !comTaskExecution.isAdHoc()) {
                    setFieldsForIndividualScheduleExecution(deviceComTasksInfo, comTaskExecution);
                } else if (comTaskExecution.isAdHoc()) {
                    setFieldsForIndividualScheduleExecution(deviceComTasksInfo, comTaskExecution);
                    deviceComTasksInfo.scheduleType = thesaurus.getFormat(DefaultTranslationKey.ON_REQUEST).format();
                }
            }


        return deviceComTasksInfo;
    }

    private void setFieldsForIndividualScheduleExecution(DeviceComTaskInfo deviceComTasksInfo, ComTaskExecution comTaskExecution) {
        deviceComTasksInfo.scheduleTypeKey = ScheduleTypeKey.INDIVIDUAL.name();
        deviceComTasksInfo.scheduleType = thesaurus.getFormat(DefaultTranslationKey.INDIVIDUAL_SCHEDULE).format();
        deviceComTasksInfo.protocolDialect = ((SingleComTaskComTaskExecution)comTaskExecution).getProtocolDialectConfigurationProperties().getDeviceProtocolDialect().getDisplayName();
        if (comTaskExecution.getNextExecutionSpecs().isPresent()) {
            deviceComTasksInfo.temporalExpression = TemporalExpressionInfo.from(comTaskExecution.getNextExecutionSpecs().get().getTemporalExpression());
        }
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
        if (comTaskExecution.usesDefaultConnectionTask()) {
            if (comTaskExecution.getConnectionTask().isPresent()) {
                deviceComTasksInfo.connectionMethod = thesaurus.getFormat(DefaultTranslationKey.DEFAULT).format() +
                        " (" + comTaskExecution.getConnectionTask().get().getName() + ")";
                deviceComTasksInfo.connectionDefinedOnDevice = true;
            } else {
                deviceComTasksInfo.connectionMethod = thesaurus.getFormat(DefaultTranslationKey.DEFAULT).format();
                deviceComTasksInfo.connectionDefinedOnDevice = false;
            }
        }
        else {
            deviceComTasksInfo.connectionMethod = comTaskExecution.getConnectionTask().get().getName();
            deviceComTasksInfo.connectionDefinedOnDevice = true;
        }
        setConnectionStrategy(deviceComTasksInfo, comTaskExecution);
        deviceComTasksInfo.urgency = comTaskExecution.getPlannedPriority();
        deviceComTasksInfo.nextCommunication = comTaskExecution.getNextExecutionTimestamp();
        deviceComTasksInfo.plannedDate = comTaskExecution.getPlannedNextExecutionTimestamp();
    }

    private CompletionCodeInfo infoFrom(CompletionCode completionCode) {
        return new CompletionCodeInfo(completionCode.name(), CompletionCodeTranslationKeys.translationFor(completionCode, thesaurus));
    }

    private void setConnectionStrategy(DeviceComTaskInfo deviceComTasksInfo, ComTaskExecution comTaskExecution) {
        if (comTaskExecution.getConnectionTask().isPresent() && comTaskExecution.getConnectionTask().get() instanceof ScheduledConnectionTask) {
            ConnectionStrategy connectionStrategy = ((ScheduledConnectionTask) comTaskExecution.getConnectionTask().get()).getConnectionStrategy();
            deviceComTasksInfo.connectionStrategy = ConnectionStrategyTranslationKeys.translationFor(connectionStrategy, thesaurus);
            deviceComTasksInfo.connectionStrategyKey = connectionStrategy.name();
        }
    }

    private void setFieldsForSharedScheduleExecution(DeviceComTaskInfo deviceComTasksInfo, ComTaskExecution comTaskExecution) {
        deviceComTasksInfo.temporalExpression = TemporalExpressionInfo.from(((ScheduledComTaskExecution) comTaskExecution).getComSchedule().getTemporalExpression());
        deviceComTasksInfo.scheduleName = ((ScheduledComTaskExecution) comTaskExecution).getComSchedule().getName();
        deviceComTasksInfo.scheduleTypeKey = ScheduleTypeKey.SHARED.name();
        deviceComTasksInfo.scheduleType = thesaurus.getFormat(DefaultTranslationKey.SHARED_SCHEDULE).format();
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
        if (comTaskExecution.usesDefaultConnectionTask()) {
            if (comTaskExecution.getConnectionTask().isPresent()) {
                deviceComTasksInfo.connectionMethod = thesaurus.getFormat(DefaultTranslationKey.DEFAULT).format() +
                        " (" + comTaskExecution.getConnectionTask().get().getName() + ")";
                deviceComTasksInfo.connectionDefinedOnDevice = true;
            } else {
                deviceComTasksInfo.connectionMethod = thesaurus.getFormat(DefaultTranslationKey.DEFAULT).format();
                deviceComTasksInfo.connectionDefinedOnDevice = false;
            }
            setConnectionStrategy(deviceComTasksInfo,comTaskExecution);
        }
        else {
            ConnectionTask<?, ?> connectionTask = comTaskExecution.getConnectionTask().orElse(null);
            deviceComTasksInfo.connectionMethod = connectionTask.getName();
            deviceComTasksInfo.connectionDefinedOnDevice = true;
            if (connectionTask instanceof ScheduledConnectionTask) {
                ScheduledConnectionTask scheduledConnectionTask = (ScheduledConnectionTask) connectionTask;
                ConnectionStrategy connectionStrategy = scheduledConnectionTask.getConnectionStrategy();
                deviceComTasksInfo.connectionStrategy = ConnectionStrategyTranslationKeys.translationFor(connectionStrategy, thesaurus);
                deviceComTasksInfo.connectionStrategyKey = connectionStrategy.name();
            }
            else {
                deviceComTasksInfo.connectionStrategy = thesaurus.getFormat(MessageSeeds.CONNECTION_TYPE_STRATEGY_NOT_APPLICABLE).format();
            }
        }
        deviceComTasksInfo.urgency = comTaskExecution.getPlannedPriority();
        deviceComTasksInfo.nextCommunication = comTaskExecution.getNextExecutionTimestamp();
        deviceComTasksInfo.plannedDate = comTaskExecution.getPlannedNextExecutionTimestamp();
    }

    private DeviceComTaskInfo from(ComTaskEnablement comTaskEnablement, Device device) {
        DeviceComTaskInfo deviceComTasksInfo = new DeviceComTaskInfo();
        deviceComTasksInfo.scheduleType = thesaurus.getFormat(DefaultTranslationKey.ON_REQUEST).format();
        deviceComTasksInfo.scheduleTypeKey = ScheduleTypeKey.ON_REQUEST.name();
        deviceComTasksInfo.comTask = ComTaskInfo.from(comTaskEnablement.getComTask());
        deviceComTasksInfo.status = thesaurus.getFormat(TaskStatusTranslationKeys.ON_HOLD).format();
        if (comTaskEnablement.usesDefaultConnectionTask()) {
            if (comTaskEnablement.getPartialConnectionTask().isPresent()) {
                PartialConnectionTask partialConnectionTask = comTaskEnablement.getPartialConnectionTask().get();

                Optional<ConnectionTask> deviceConnectionTaskOptional = findDefaultConnectionTaskInCompleteTopology(device);
                if (deviceConnectionTaskOptional.isPresent()) {
                    deviceComTasksInfo.connectionMethod = thesaurus.getFormat(DefaultTranslationKey.DEFAULT).format() +
                            " (" + deviceConnectionTaskOptional.get().getName() + ")";
                    deviceComTasksInfo.connectionDefinedOnDevice = true;
                } else {
                    deviceComTasksInfo.connectionMethod = thesaurus.getFormat(DefaultTranslationKey.DEFAULT).format() +
                            " (" + partialConnectionTask.getName() + ")";
                    deviceComTasksInfo.connectionDefinedOnDevice = false;
                }

                if (partialConnectionTask instanceof PartialScheduledConnectionTask) {
                    ConnectionStrategy connectionStrategy = ((PartialScheduledConnectionTask) partialConnectionTask).getConnectionStrategy();
                    deviceComTasksInfo.connectionStrategy = ConnectionStrategyTranslationKeys.translationFor(connectionStrategy, thesaurus);
                    deviceComTasksInfo.connectionStrategyKey = connectionStrategy.name();
                }

            } else {
                Optional<ConnectionTask> deviceConnectionTaskOptional = findDefaultConnectionTaskInCompleteTopology(device);
                if (deviceConnectionTaskOptional.isPresent()) {
                    deviceComTasksInfo.connectionMethod = thesaurus.getFormat(DefaultTranslationKey.DEFAULT).format() +
                            " (" + deviceConnectionTaskOptional.get().getName() + ")";
                    deviceComTasksInfo.connectionDefinedOnDevice = true;
                } else {
                    deviceComTasksInfo.connectionMethod = thesaurus.getFormat(DefaultTranslationKey.DEFAULT).format();
                    deviceComTasksInfo.connectionDefinedOnDevice = false;
                }
            }
        } else {
            if (comTaskEnablement.getPartialConnectionTask().isPresent()) {
                PartialConnectionTask partialConnectionTask = comTaskEnablement.getPartialConnectionTask().get();
                Optional<ConnectionTask<?, ?>> deviceConnectionTaskOptional = device.getConnectionTasks().stream().filter(connectionTask -> connectionTask.getName().equals(partialConnectionTask.getName())).findFirst();
                if (deviceConnectionTaskOptional.isPresent()) {
                    deviceComTasksInfo.connectionMethod = partialConnectionTask.getName();
                    deviceComTasksInfo.connectionDefinedOnDevice = true;
                } else {
                    deviceComTasksInfo.connectionMethod = partialConnectionTask.getName();
                    deviceComTasksInfo.connectionDefinedOnDevice = false;
                }

                if (partialConnectionTask instanceof PartialScheduledConnectionTask) {
                    ConnectionStrategy connectionStrategy = ((PartialScheduledConnectionTask) partialConnectionTask).getConnectionStrategy();
                    deviceComTasksInfo.connectionStrategy = ConnectionStrategyTranslationKeys.translationFor(connectionStrategy, thesaurus);
                    deviceComTasksInfo.connectionStrategyKey = connectionStrategy.name();
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