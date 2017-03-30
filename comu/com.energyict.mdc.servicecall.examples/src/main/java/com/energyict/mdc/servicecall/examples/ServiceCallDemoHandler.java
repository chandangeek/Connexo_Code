/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.servicecall.examples;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(name = "com.energyict.mdc.servicecall.example.servicecalldemohandler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=ServiceCallDemoHandler")
public class ServiceCallDemoHandler implements ServiceCallHandler {

    public ServiceCallDemoHandler() {
    }

    @Activate
    public void activate() {
        System.out.println("Activating Service call demo handler");
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINEST, "Now entering state " + newState.getKey());
    }
}
