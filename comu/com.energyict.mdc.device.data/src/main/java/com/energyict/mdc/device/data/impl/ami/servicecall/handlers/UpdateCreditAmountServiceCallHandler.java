/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami.servicecall.handlers;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.ami.CompletionMessageInfo;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.common.tasks.PriorityComTaskExecutionLink;
import com.energyict.mdc.common.tasks.StatusInformationTask;
import com.energyict.mdc.device.data.CreditAmount;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.ami.CompletionOptionsCallBack;
import com.energyict.mdc.device.data.exceptions.NoSuchElementException;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandOperationStatus;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandServiceCallDomainExtension;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.PriorityComTaskService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.upl.messages.DeviceMessageAttribute;

import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component(name = "com.energyict.servicecall.ami.creditamount.handler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=" + UpdateCreditAmountServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
public class UpdateCreditAmountServiceCallHandler extends AbstractOperationServiceCallHandler {

    public static final String VERSION = "v1.0";
    public static final String SERVICE_CALL_HANDLER_NAME = "UpdateCreditAmountServiceCallHandler";

    private volatile DeviceService deviceService;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile PriorityComTaskService priorityComTaskService;
    private volatile EngineConfigurationService engineConfigurationService;
    private volatile DeviceMessageService deviceMessageService;

    public UpdateCreditAmountServiceCallHandler() {
        super();
    }

    // Constructor only to be used in JUnit tests
    public UpdateCreditAmountServiceCallHandler(MessageService messageService, DeviceService deviceService, Thesaurus thesaurus,
                                                CompletionOptionsCallBack completionOptionsCallBack, CommunicationTaskService communicationTaskService,
                                                EngineConfigurationService engineConfigurationService, PriorityComTaskService priorityComTaskService,
                                                DeviceMessageService deviceMessageService) {
        super.setMessageService(messageService);
        this.setDeviceService(deviceService);
        super.setThesaurus(thesaurus);
        super.setCompletionOptionsCallBack(completionOptionsCallBack);
        this.setCommunicationTaskService(communicationTaskService);
        this.setEngineConfigurationService(engineConfigurationService);
        this.setPriorityComTaskService(priorityComTaskService);
        this.setDeviceMessageService(deviceMessageService);

    }

    public static final String APPLICATION = "MDC";

    @Reference
    public void setMessageService(MessageService messageService) {
        super.setMessageService(messageService);
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }

    @Reference
    public void setPriorityComTaskService(PriorityComTaskService priorityComTaskService) {
        this.priorityComTaskService = priorityComTaskService;
    }

    @Reference
    public void setEngineConfigurationService(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }

    @Reference
    public void setDeviceMessageService(DeviceMessageService deviceMessageService) {
        this.deviceMessageService = deviceMessageService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        super.setThesaurus(nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference
    protected void setCompletionOptionsCallBack(CompletionOptionsCallBack completionOptionsCallBack) {
        super.setCompletionOptionsCallBack(completionOptionsCallBack);
    }

    @Override
    protected void handleAllDeviceCommandsExecutedSuccessfully(ServiceCall serviceCall, CommandServiceCallDomainExtension domainExtension) {
        triggerStatusInformationTask(serviceCall, domainExtension);
        serviceCall.requestTransition(DefaultState.WAITING);
    }

    private void triggerStatusInformationTask(ServiceCall serviceCall, CommandServiceCallDomainExtension domainExtension) {
        serviceCall.log(LogLevel.INFO, "Scheduling 'Status information' task to verify credit amount");
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

        ComTaskExecution lockedComTaskExecution = getLockedComTaskExecution(existingComTaskExecution.getId())
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
                .filter(cte -> cte.getComTask().getProtocolTasks().stream()
                        .anyMatch(StatusInformationTask.class::isInstance))
                .findAny();
        if (enablementOptional.isPresent()) {
            return enablementOptional.get();
        } else {
            serviceCall.log(LogLevel.SEVERE, MessageSeeds.NO_STATUS_INFORMATION_COMTASK.getDefaultFormat());
            getCompletionOptionsCallBack().sendFinishedMessageToDestinationSpec(serviceCall, CompletionMessageInfo.CompletionMessageStatus.FAILURE, CompletionMessageInfo.FailureReason.NO_COMTASK_TO_VERIFY_BREAKER_STATUS);
            serviceCall.requestTransition(DefaultState.FAILED);
            throw new IllegalStateException(getThesaurus().getFormat(MessageSeeds.NO_STATUS_INFORMATION_COMTASK).format());
        }
    }

    @Override
    protected void verifyDeviceStatus(ServiceCall serviceCall) {
        Device device = (Device) serviceCall.getTargetObject().get();
        Optional<CreditAmount> creditAmount = deviceService.getCreditAmount(device);
        CreditAmount desiredCreditAmount = getDesiredCreditAmount(device, serviceCall);
        if (creditAmount.isPresent()) {
            if (creditAmount.get().getCreditAmount().equals(desiredCreditAmount.getCreditAmount())
                    && creditAmount.get().getCreditType().equals(desiredCreditAmount.getCreditType())) {
                serviceCall.log(LogLevel.INFO, MessageFormat.format("Confirmed device credit amount: {0} {1}", creditAmount.get().getCreditAmount(), creditAmount.get().getCreditType()));
                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
            } else {
                serviceCall.log(LogLevel.SEVERE, MessageFormat.format("Device credit amount {0} {1} doesn''t match expected amount {2} {3}",
                        creditAmount.get().getCreditAmount(), creditAmount.get().getCreditType(),
                        desiredCreditAmount.getCreditAmount(), desiredCreditAmount.getCreditType())
                );
                getCompletionOptionsCallBack().sendFinishedMessageToDestinationSpec(serviceCall, CompletionMessageInfo.CompletionMessageStatus.FAILURE, CompletionMessageInfo.FailureReason.INCORRECT_DEVICE_BREAKER_STATUS);
                serviceCall.requestTransition(DefaultState.FAILED);
            }
        } else {
            //Else in case no credit amount is present, put the ServiceCall again in waiting state
            serviceCall.requestTransition(DefaultState.WAITING);
        }
    }

    private CreditAmount getDesiredCreditAmount(Device device, ServiceCall serviceCall) {
        String creditType = null;
        BigDecimal creditAmount = null;
        CommandServiceCallDomainExtension domainExtension = serviceCall.getExtensionFor(new CommandCustomPropertySet()).get();
        Optional<Long> deviceMessageIdOptional = domainExtension.getDeviceMessageIds().stream().findFirst();
        if (deviceMessageIdOptional.isPresent()) {
            Optional<DeviceMessage> deviceMessageOptional = deviceMessageService.findDeviceMessageById(deviceMessageIdOptional.get());
            if (deviceMessageOptional.isPresent()) {
                List<? extends DeviceMessageAttribute> attributes = deviceMessageOptional.get().getAttributes();
                Optional<? extends DeviceMessageAttribute> creditTypeOptional = attributes.stream().filter(attr -> attr.getName().equals(DeviceMessageConstants.creditType)).findFirst();
                if (creditTypeOptional.isPresent()) {
                    creditType = (String) creditTypeOptional.get().getValue();
                }
                Optional<? extends DeviceMessageAttribute> creditAmountOptional = attributes.stream().filter(attr -> attr.getName().equals(DeviceMessageConstants.creditAmount)).findFirst();
                if (creditAmountOptional.isPresent()) {
                    creditAmount = (BigDecimal) creditAmountOptional.get().getValue();
                }
            }
        }
        return deviceService.creditAmountFrom(device, creditType, creditAmount);
    }

    private Optional<ComTaskExecution> getLockedComTaskExecution(long id) {
        return communicationTaskService.findAndLockComTaskExecutionById(id)
                .filter(candidate -> !candidate.isObsolete());
    }

    private boolean canRunWithPriority(ComTaskExecution comTaskExecution) {
        return comTaskExecution.getConnectionTask()
                .filter(connectionTask -> Objects.nonNull(connectionTask.getComPortPool()) &&
                        engineConfigurationService.calculateMaxPriorityConnections(connectionTask.getComPortPool(), connectionTask.getComPortPool().getPctHighPrioTasks()) > 0)
                .isPresent();
    }
}
