/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami.servicecall.handlers;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.common.tasks.PriorityComTaskExecutionLink;
import com.energyict.mdc.common.tasks.StatusInformationTask;
import com.energyict.mdc.device.data.ActivatedBreakerStatus;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.exceptions.NoSuchElementException;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandOperationStatus;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandServiceCallDomainExtension;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.PriorityComTaskService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.upl.meterdata.BreakerStatus;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;

import static com.elster.jupiter.metering.ami.CompletionMessageInfo.CompletionMessageStatus;
import static com.elster.jupiter.metering.ami.CompletionMessageInfo.FailureReason;

/**
 * Abstract implementation of {@link ServiceCallHandler} interface which handles the different steps for
 * the connect/dicsonnect/arm of the devices breaker.
 *
 * @author sva
 * @since 06/06/16 - 13:05
 */
public abstract class AbstractContactorOperationServiceCallHandler extends AbstractOperationServiceCallHandler {

    private volatile DeviceService deviceService;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile PriorityComTaskService priorityComTaskService;
    private volatile EngineConfigurationService engineConfigurationService;

    public static final String APPLICATION = "MDC";

    public AbstractContactorOperationServiceCallHandler() {
    }

    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }

    public void setPriorityComTaskService(PriorityComTaskService priorityComTaskService) {
        this.priorityComTaskService = priorityComTaskService;
    }

    public void setEngineConfigurationService(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }

    @Override
    protected void handleAllDeviceCommandsExecutedSuccessfully(ServiceCall serviceCall, CommandServiceCallDomainExtension domainExtension) {
        triggerStatusInformationTask(serviceCall, domainExtension);
        serviceCall.requestTransition(DefaultState.WAITING);
    }

    private void triggerStatusInformationTask(ServiceCall serviceCall, CommandServiceCallDomainExtension domainExtension) {
        serviceCall.log(LogLevel.INFO, "Scheduling 'Status information' task to verify the breaker status");
        Device device = (Device) serviceCall.getTargetObject().get();
        domainExtension.setCommandOperationStatus(CommandOperationStatus.READ_STATUS_INFORMATION);
        serviceCall.update(domainExtension);

        boolean withPriority = domainExtension.isRunWithPriority();

        ComTaskEnablement comTaskEnablement = getStatusInformationComTaskEnablement(device, serviceCall);
        Optional<ComTaskExecution> optionalExistingComTaskExecution = device.getComTaskExecutions().stream()
                .filter(cte -> cte.getComTask().getId() == comTaskEnablement.getComTask().getId())
                .findFirst();
        if (optionalExistingComTaskExecution.isPresent() && optionalExistingComTaskExecution.get().isOnHold()) {
            throw new IllegalStateException(getThesaurus().getFormat(MessageSeeds.NO_STATUS_INFORMATION_COMTASK).format());
        }
        ComTaskExecution existingComTaskExecution = optionalExistingComTaskExecution.orElseGet(() -> createAdHocComTaskExecution(device, comTaskEnablement));

        if (withPriority && !canRunWithPriority(existingComTaskExecution)) {
            throw NoSuchElementException.comTaskToRunWithPriorityCouldNotBeLocated(getThesaurus()).get();
        }

        ComTaskExecution lockedComTaskExecution = getLockedComTaskExecution(existingComTaskExecution.getId(), existingComTaskExecution.getVersion())
                .orElseThrow(() -> new IllegalStateException(getThesaurus().getFormat(MessageSeeds.NO_SUCH_COM_TASK_EXECUTION).format(existingComTaskExecution.getId())));

        if (withPriority) {
            Optional<PriorityComTaskExecutionLink> priorityComTaskExecutionLink = priorityComTaskService.findByComTaskExecution(lockedComTaskExecution);
            priorityComTaskExecutionLink.orElseGet(() -> priorityComTaskService.from(lockedComTaskExecution));
        }

        lockedComTaskExecution.addNewComTaskExecutionTrigger(domainExtension.getReleaseDate());
        lockedComTaskExecution.updateNextExecutionTimestamp();
    }

    private ComTaskExecution createAdHocComTaskExecution(Device device, ComTaskEnablement comTaskEnablement) {
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        if (comTaskEnablement.hasPartialConnectionTask()) {
            device.getConnectionTasks().stream()
                    .filter(connectionTask -> connectionTask.getPartialConnectionTask().getId() == comTaskEnablement.getPartialConnectionTask().get().getId())
                    .findFirst()
                    .ifPresent(comTaskExecutionBuilder::connectionTask);
        }
        ComTaskExecution manuallyScheduledComTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        return manuallyScheduledComTaskExecution;
    }

    private ComTaskEnablement getStatusInformationComTaskEnablement(Device device, ServiceCall serviceCall) {
        Optional<ComTaskEnablement> enablementOptional = device.getDeviceConfiguration()
                .getComTaskEnablements()
                .stream()
                .filter(cte -> cte.getComTask().isManualSystemTask())
                .filter(cte -> cte.getComTask().getProtocolTasks().stream().
                        filter(task -> task instanceof StatusInformationTask).
                        findFirst().
                        isPresent())
                .findAny();
        if (enablementOptional.isPresent()) {
            return enablementOptional.get();
        } else {
            serviceCall.log(LogLevel.SEVERE, MessageSeeds.NO_STATUS_INFORMATION_COMTASK.getDefaultFormat());
            getCompletionOptionsCallBack().sendFinishedMessageToDestinationSpec(serviceCall, CompletionMessageStatus.FAILURE, FailureReason.NO_COMTASK_TO_VERIFY_BREAKER_STATUS);
            serviceCall.requestTransition(DefaultState.FAILED);
            throw new IllegalStateException(getThesaurus().getFormat(MessageSeeds.NO_STATUS_INFORMATION_COMTASK).format());
        }
    }

    @Override
    protected void verifyDeviceStatus(ServiceCall serviceCall) {
        Device device = (Device) serviceCall.getTargetObject().get();
        Optional<ActivatedBreakerStatus> breakerStatus = deviceService.getActiveBreakerStatus(device);
        if (breakerStatus.isPresent()) {
            if (breakerStatus.get().getBreakerStatus().equals(getDesiredBreakerStatus())) {
                serviceCall.log(LogLevel.INFO, MessageFormat.format("Confirmed device breaker status: {0}", breakerStatus.get().getBreakerStatus()));
                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
            } else {
                serviceCall.log(LogLevel.SEVERE, MessageFormat.format("Device breaker status {0} doesn''t match expected status {1}",
                        breakerStatus.get().getBreakerStatus(),
                        getDesiredBreakerStatus())
                );
                getCompletionOptionsCallBack().sendFinishedMessageToDestinationSpec(serviceCall, CompletionMessageStatus.FAILURE, FailureReason.INCORRECT_DEVICE_BREAKER_STATUS);
                serviceCall.requestTransition(DefaultState.FAILED);
            }
        } else {
            //Else in case no active breaker status is present, put the ServiceCall again in waiting state
            serviceCall.requestTransition(DefaultState.WAITING);
        }
    }

    protected abstract BreakerStatus getDesiredBreakerStatus();

    private Optional<ComTaskExecution> getLockedComTaskExecution(long id, long version) {
        return communicationTaskService.findAndLockComTaskExecutionByIdAndVersion(id, version)
                .filter(candidate -> !candidate.isObsolete());
    }

    private boolean canRunWithPriority(ComTaskExecution comTaskExecution) {
        return comTaskExecution.getConnectionTask()
                .filter(connectionTask -> Objects.nonNull(connectionTask.getComPortPool()) &&
                        engineConfigurationService.calculateMaxPriorityConnections(connectionTask.getComPortPool(), connectionTask.getComPortPool().getPctHighPrioTasks()) > 0)
                .isPresent();
    }
}