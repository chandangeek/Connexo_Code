/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami.servicecall.handlers;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.ActivatedBreakerStatus;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandOperationStatus;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandServiceCallDomainExtension;
import com.energyict.mdc.upl.meterdata.BreakerStatus;

import java.text.MessageFormat;
import java.util.Optional;

import static com.elster.jupiter.metering.ami.CompletionMessageInfo.CompletionMessageStatus;
import static com.elster.jupiter.metering.ami.CompletionMessageInfo.FailureReason;

/**
 * Abstract implementation of {@link ServiceCallHandler} interface which handles the different steps for
 * the connect/disconnect/arm of the device breaker.
 *
 * @author sva
 * @since 06/06/16 - 13:05
 */
public abstract class AbstractContactorOperationServiceCallHandler extends AbstractOperationServiceCallHandler {
    public static final String APPLICATION = "MDC";

    private volatile DeviceService deviceService;

    public AbstractContactorOperationServiceCallHandler() {
    }

    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    protected void handleAllDeviceCommandsExecutedSuccessfully(ServiceCall serviceCall, CommandServiceCallDomainExtension domainExtension) {
        switchToReadStatusInformation(serviceCall, domainExtension);
        serviceCall.requestTransition(DefaultState.WAITING);
        serviceCall.requestTransition(DefaultState.ONGOING);
    }

    private void switchToReadStatusInformation(ServiceCall serviceCall, CommandServiceCallDomainExtension domainExtension) {
        serviceCall.log(LogLevel.INFO, "Verifying the breaker status via register");
        domainExtension.setCommandOperationStatus(CommandOperationStatus.READ_STATUS_INFORMATION);
        serviceCall.update(domainExtension);
    }

    @Override
    protected void verifyDeviceStatus(ServiceCall serviceCall) {
        Device device = (Device) serviceCall.getTargetObject().get();
        Optional<ActivatedBreakerStatus> breakerStatus = deviceService.getActiveBreakerStatus(device);

        if (!breakerStatus.isPresent()) {
            serviceCall.log(LogLevel.SEVERE, "Device doesn't have a relevant register");
            getCompletionOptionsCallBack().sendFinishedMessageToDestinationSpec(serviceCall, CompletionMessageStatus.FAILURE, FailureReason.INCORRECT_DEVICE_BREAKER_STATUS);
            serviceCall.requestTransition(DefaultState.FAILED);
        } else {
            if (breakerStatus.get().getBreakerStatus().equals(getDesiredBreakerStatus())) {
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
    }

    protected abstract BreakerStatus getDesiredBreakerStatus();
}
