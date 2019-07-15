/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;

public class SubParentGetMeterReadingsServiceCallHandler implements ServiceCallHandler {
    public static final String SERVICE_CALL_HANDLER_NAME = "SubParentGetMeterReadingsServiceCallHandler";
    public static final String VERSION = "v1.0";


    public SubParentGetMeterReadingsServiceCallHandler() {
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Service call is switched to state " + newState.getDefaultFormat());
        // Do nothing
    }

    @Override
    public void onChildStateChange(ServiceCall subParentServiceCall, ServiceCall childServiceCall, DefaultState oldState, DefaultState newState) {
        switch (newState) {
            case SUCCESSFUL:
            case PARTIAL_SUCCESS:
            case CANCELLED:
            case FAILED:
                if (subParentServiceCall.getState() == DefaultState.WAITING) {
                    ServiceCallTransitionUtils.resultTransition(subParentServiceCall);
                }
                break;
        }
    }
}
