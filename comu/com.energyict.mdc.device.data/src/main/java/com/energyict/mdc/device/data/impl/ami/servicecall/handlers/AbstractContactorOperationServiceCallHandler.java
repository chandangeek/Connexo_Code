/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami.servicecall.handlers;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.Register;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandOperationStatus;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandServiceCallDomainExtension;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.PriorityComTaskService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.upl.meterdata.BreakerStatus;
import com.energyict.obis.ObisCode;

import java.text.MessageFormat;
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
    private static final ObisCode BREAKER_STATUS = ObisCode.fromString("0.0.96.3.10.255");

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
        switchToReadStatusInformation(serviceCall, domainExtension);
        serviceCall.requestTransition(DefaultState.WAITING);
        serviceCall.requestTransition(DefaultState.ONGOING);
    }

    private void  switchToReadStatusInformation(ServiceCall serviceCall, CommandServiceCallDomainExtension domainExtension) {
        serviceCall.log(LogLevel.INFO, "Verifying the breaker status via register");
        domainExtension.setCommandOperationStatus(CommandOperationStatus.READ_STATUS_INFORMATION);
        serviceCall.update(domainExtension);
    }

    @Override
    protected void verifyDeviceStatus(ServiceCall serviceCall) {
        Device device = (Device) serviceCall.getTargetObject().get();
        Optional<BreakerStatus> breakerStatus = getActiveBreakerStatus(device);
        if (!breakerStatus.isPresent()) {
            serviceCall.log(LogLevel.SEVERE, "Device doesn''t have register of type Contactor status");
            getCompletionOptionsCallBack().sendFinishedMessageToDestinationSpec(serviceCall, CompletionMessageStatus.FAILURE, FailureReason.INCORRECT_DEVICE_BREAKER_STATUS);
            serviceCall.requestTransition(DefaultState.FAILED);
            return;
        }
        if (breakerStatus.get().equals(getDesiredBreakerStatus())) {
            serviceCall.log(LogLevel.INFO, MessageFormat.format("Confirmed device breaker status: {0}", breakerStatus));
            serviceCall.requestTransition(DefaultState.SUCCESSFUL);
       } else {
            serviceCall.log(LogLevel.SEVERE, MessageFormat.format("Device breaker status {0} doesn''t match expected status {1}",
            breakerStatus,
            getDesiredBreakerStatus()));
            getCompletionOptionsCallBack().sendFinishedMessageToDestinationSpec(serviceCall, CompletionMessageStatus.FAILURE, FailureReason.INCORRECT_DEVICE_BREAKER_STATUS);
            serviceCall.requestTransition(DefaultState.FAILED);
       }
    }

    private Optional<BreakerStatus> getActiveBreakerStatus(Device device) {
        return device.getRegisters().stream()
                .filter(register -> register.getRegisterTypeObisCode().equals(BREAKER_STATUS))
                .findAny()
                .map(Register::getLastReading)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(NumericalReading.class::cast)
                .map(val -> val.getValue().intValue() == 1 ? BreakerStatus.CONNECTED : BreakerStatus.DISCONNECTED);
    }

    protected abstract BreakerStatus getDesiredBreakerStatus();
}