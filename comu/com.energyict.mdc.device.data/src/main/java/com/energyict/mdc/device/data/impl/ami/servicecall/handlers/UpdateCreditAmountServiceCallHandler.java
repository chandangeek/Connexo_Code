/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami.servicecall.handlers;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ami.CompletionMessageInfo;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.Reading;
import com.energyict.mdc.common.device.data.Register;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.device.data.*;
import com.energyict.mdc.device.data.ami.CompletionOptionsCallBack;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandOperationStatus;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandServiceCallDomainExtension;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.PriorityComTaskService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.upl.messages.DeviceMessageAttribute;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

@Component(name = "com.energyict.servicecall.ami.creditamount.handler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=" + UpdateCreditAmountServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
public class UpdateCreditAmountServiceCallHandler extends AbstractOperationServiceCallHandler {

    public static final String VERSION = "v1.0";
    public static final String SERVICE_CALL_HANDLER_NAME = "UpdateCreditAmountServiceCallHandler";
    private static final ObisCode IMPORT_CREDIT = ObisCode.fromString("0.0.19.10.0.255");
    private static final ObisCode EMERGENCY_CREDIT = ObisCode.fromString("0.0.19.10.1.255");

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
        String desiredCreditTypeObisCode = desiredCreditAmount.getCreditType().equals("Import Credit") ? IMPORT_CREDIT.getValue() : EMERGENCY_CREDIT.getValue();
        Optional<Register> register = getRegister(device, desiredCreditTypeObisCode);

        if (!register.isPresent()) {
            serviceCall.log(LogLevel.SEVERE,  MessageFormat.format("Device doesn''t have register of type {0}", desiredCreditAmount.getCreditType()));
            getCompletionOptionsCallBack().sendFinishedMessageToDestinationSpec(serviceCall, CompletionMessageInfo.CompletionMessageStatus.FAILURE, CompletionMessageInfo.FailureReason.INCORRECT_CREDIT_AMOUNT);
            serviceCall.requestTransition(DefaultState.FAILED);
        }

        List<Reading> readingsList =(List<Reading>) register.get()
                .getReadings(Interval.forever())
                .stream()
                .sorted(Comparator.comparing(Reading::getTimeStamp).reversed())
                .collect(Collectors.toList());

        Optional<Integer> currentValue = getCurrentCreditAmount(readingsList);
        Optional<Integer> previousValue = getPreviousCreditAmount(readingsList);

            if (previousValue.isPresent() && currentValue.isPresent()) {
                if (previousValue.get() + desiredCreditAmount.getCreditAmount().intValue() == currentValue.get()) {
                    serviceCall.log(LogLevel.INFO, MessageFormat.format("Confirmed device credit amount: {0} of type {1}.", currentValue.get(), desiredCreditAmount.getCreditType()));
                    serviceCall.requestTransition(DefaultState.SUCCESSFUL);
                } else {
                    serviceCall.log(LogLevel.SEVERE, MessageFormat.format("Device credit amount {0} doesn''t match the expected amount: {1} of type {2}.",
                            currentValue.get(),
                            previousValue.get() + desiredCreditAmount.getCreditAmount().intValue(), desiredCreditAmount.getCreditType())
                    );
                    getCompletionOptionsCallBack().sendFinishedMessageToDestinationSpec(serviceCall, CompletionMessageInfo.CompletionMessageStatus.FAILURE, CompletionMessageInfo.FailureReason.INCORRECT_CREDIT_AMOUNT);
                    serviceCall.requestTransition(DefaultState.FAILED);
                }
            } else {
                serviceCall.log(LogLevel.SEVERE, MessageFormat.format("Device credit amount of credit type {0} is absent", desiredCreditAmount.getCreditType()));
                getCompletionOptionsCallBack().sendFinishedMessageToDestinationSpec(serviceCall, CompletionMessageInfo.CompletionMessageStatus.FAILURE, CompletionMessageInfo.FailureReason.INCORRECT_CREDIT_AMOUNT);
                serviceCall.requestTransition(DefaultState.FAILED);
            }
    }

    private Optional<Integer> getPreviousCreditAmount(List<Reading> readings) {
        return readings.stream()
                .skip(1)
                .findFirst()
                .map(Reading::getActualReading)
                .map(ReadingRecord::getValue)
                .map(BigDecimal::intValue);
    }

    private Optional<Integer> getCurrentCreditAmount(List<Reading> readings) {
        return readings.stream()
                .findFirst()
                .map(Reading::getActualReading)
                .map(ReadingRecord::getValue)
                .map(BigDecimal::intValue);
    }

    private Optional<Register> getRegister(Device device, String desiredCreditType) {
        return Stream.of(device)
                .filter(Objects::nonNull)
                .map(Device::getRegisters)
                .flatMap(List::stream)
                .filter(register -> register.getDeviceObisCode().getValue().equals(desiredCreditType))
                .findFirst();
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
