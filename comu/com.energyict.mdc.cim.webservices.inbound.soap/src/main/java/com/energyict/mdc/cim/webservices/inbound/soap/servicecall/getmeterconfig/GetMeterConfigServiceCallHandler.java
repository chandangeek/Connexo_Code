/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterconfig;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import org.osgi.service.component.annotations.Component;


/**
 * Implementation of {@link ServiceCallHandler} interface which handles the different steps for CIM WS GetMeterConfig
 */
@Component(name = "com.energyict.mdc.cim.webservices.inbound.soap.servicecall.GetMeterConfigServiceCallHandler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=" + GetMeterConfigServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
public class GetMeterConfigServiceCallHandler implements ServiceCallHandler {
    public static final String SERVICE_CALL_HANDLER_NAME = "GetMeterConfigServiceCallHandler";
    public static final String VERSION = "v1.0";

    public GetMeterConfigServiceCallHandler() {

    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case ONGOING:
                processMeterConfigServiceCall(serviceCall);
                break;
            case SUCCESSFUL:
                break;
            case FAILED:
                break;
            case PENDING:
                serviceCall.requestTransition(DefaultState.ONGOING);
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    private void processMeterConfigServiceCall(ServiceCall serviceCall)  {
         serviceCall.requestTransition(DefaultState.SUCCESSFUL);
    }

}
