/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami.servicecall.handlers;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ami.CompletionMessageInfo;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.Register;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.CreditAmount;
import com.energyict.mdc.device.data.ami.CompletionOptionsCallBack;
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
import java.time.Clock;
import java.util.Optional;
import java.util.List;

import java.util.stream.Stream;

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
    private volatile Clock clock;

    public UpdateCreditAmountServiceCallHandler() {
        super();
    }

    // Constructor only to be used in JUnit tests
    public UpdateCreditAmountServiceCallHandler(MessageService messageService, DeviceService deviceService, Thesaurus thesaurus,
                                                CompletionOptionsCallBack completionOptionsCallBack, CommunicationTaskService communicationTaskService,
                                                EngineConfigurationService engineConfigurationService, PriorityComTaskService priorityComTaskService,
                                                DeviceMessageService deviceMessageService, Clock clock) {
        super.setMessageService(messageService);
        this.setDeviceService(deviceService);
        super.setThesaurus(thesaurus);
        super.setCompletionOptionsCallBack(completionOptionsCallBack);
        this.setCommunicationTaskService(communicationTaskService);
        this.setEngineConfigurationService(engineConfigurationService);
        this.setPriorityComTaskService(priorityComTaskService);
        this.setDeviceMessageService(deviceMessageService);
        this.setClock(clock);
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
    public void setClock(Clock clock) {
        this.clock = clock;
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
        switchToReadStatusInformation(serviceCall, domainExtension);
        serviceCall.requestTransition(DefaultState.WAITING);
        serviceCall.requestTransition(DefaultState.ONGOING);
    }

    private void switchToReadStatusInformation(ServiceCall serviceCall, CommandServiceCallDomainExtension domainExtension) {
        serviceCall.log(LogLevel.INFO, "Verifying credit amount via register");
        domainExtension.setCommandOperationStatus(CommandOperationStatus.READ_STATUS_INFORMATION);
        serviceCall.update(domainExtension);
    }

    @Override
    protected void verifyDeviceStatus(ServiceCall serviceCall) {
        Device device = (Device) serviceCall.getTargetObject().get();
        CreditAmount desiredCreditAmount = getDesiredCreditAmount(device, serviceCall);
        String desiredCreditTypeObisCode = desiredCreditAmount.getCreditType().equals("Import Credit") ? CreditAmount.IMPORT_CREDIT.getValue() : CreditAmount.EMERGENCY_CREDIT.getValue();

        Optional<ReadingType> readingType = getReadingType(device, desiredCreditTypeObisCode);
        List<? extends BaseReadingRecord> readings;

        if (!readingType.isPresent() || (readings = getComparableReadings(device, readingType.get())).isEmpty()) {
            serviceCall.log(LogLevel.SEVERE, "Device doesn' t have a relevant register");
            getCompletionOptionsCallBack().sendFinishedMessageToDestinationSpec(serviceCall, CompletionMessageInfo.CompletionMessageStatus.FAILURE, CompletionMessageInfo.FailureReason.INCORRECT_CREDIT_AMOUNT);
            serviceCall.requestTransition(DefaultState.FAILED);
        } else {
            BigDecimal currentValue = readings.get(0).getValue();
            BigDecimal previousValue = readings.size() != 2 ? BigDecimal.ZERO : readings.get(1).getValue();
            if (previousValue.add(desiredCreditAmount.getCreditAmount()).equals(currentValue)) {
                serviceCall.log(LogLevel.INFO, MessageFormat.format("Confirmed device credit amount: {0} of type {1}.", currentValue, desiredCreditAmount.getCreditType()));
                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
            } else {
                serviceCall.log(LogLevel.SEVERE,
                        MessageFormat.format("Device credit amount {0} doesn''t match the expected amount: {1} of type {2}.",
                                currentValue,
                                (previousValue.add(desiredCreditAmount.getCreditAmount())), desiredCreditAmount.getCreditType())
                );
                getCompletionOptionsCallBack().sendFinishedMessageToDestinationSpec(serviceCall, CompletionMessageInfo.CompletionMessageStatus.FAILURE, CompletionMessageInfo.FailureReason.INCORRECT_CREDIT_AMOUNT);
                serviceCall.requestTransition(DefaultState.FAILED);
            }
        }
    }

    private List<? extends BaseReadingRecord> getComparableReadings(Device device, ReadingType readingType) {
        return device.getMeter().getReadingsBefore(clock.instant(), readingType, 2);
    }

    private Optional<ReadingType> getReadingType(Device device, String desiredCreditType) {
        return Stream.of(device)
                .map(Device::getRegisters)
                .flatMap(List::stream)
                .filter(register -> register.getDeviceObisCode().getValue().equals(desiredCreditType))
                .findFirst()
                .map(Register::getReadingType);
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
}
