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

public class SubMasterEndDeviceControlsServiceCallHandler implements ServiceCallHandler {
    public static final String SERVICE_CALL_HANDLER_NAME = "SubMasterEndDeviceControls";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

    private final ServiceCallService serviceCallService;

    @Inject
    public SubMasterEndDeviceControlsServiceCallHandler(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Service call is switched to state " + newState.getDefaultFormat());
    }

    @Override
    public void onChildStateChange(ServiceCall parentServiceCall, ServiceCall subParentServiceCall, DefaultState oldState, DefaultState newState) {
        ServiceCallTransitionUtils.resultTransition(parentServiceCall, serviceCallService);
    }
}
