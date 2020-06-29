/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.enddevicecontrols;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;

import javax.inject.Inject;

public class EndDeviceControlsServiceCallHandler implements ServiceCallHandler {
    public static final String SERVICE_CALL_HANDLER_NAME = "EndDeviceControls";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

    private final ServiceCallService serviceCallService;

    @Inject
    public EndDeviceControlsServiceCallHandler(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        if (newState.equals(DefaultState.CANCELLED)) {
            EndDeviceControlsDomainExtension extension = serviceCall
                    .getExtension(EndDeviceControlsDomainExtension.class)
                    .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
            if (extension.getCancellationReason() == CancellationReason.NOT_CANCELLED) {
                extension.setCancellationReason(CancellationReason.MANUALLY);
                serviceCall.update(extension);
            }
        }
    }

    @Override
    public void onChildStateChange(ServiceCall parentServiceCall, ServiceCall subParentServiceCall, DefaultState oldState, DefaultState newState) {
        ServiceCallTransitionUtils.resultTransition(parentServiceCall, serviceCallService);
    }
}
