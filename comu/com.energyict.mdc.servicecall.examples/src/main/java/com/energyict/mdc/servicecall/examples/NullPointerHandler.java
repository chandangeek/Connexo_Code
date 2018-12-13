/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.servicecall.examples;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * This handler will throw a null pointer
 */
@Component(name = "com.energyict.mdc.servicecall.example.nullpointer",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=NullPointerHandler")
public class NullPointerHandler implements ServiceCallHandler {

    private volatile CustomPropertySetService customPropertySetService;

    public NullPointerHandler() {
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Activate
    public void activate() {
        System.out.println("Activating null pointer handler");
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINEST, "Now entering state " + newState.getKey());
        switch (newState) {
            case ONGOING:
                throw new NullPointerException("Roses are red, violets are blue, you messed with handlers, and now I mess with you");
            case PENDING:
                serviceCall.requestTransition(DefaultState.ONGOING);
                break;
            default:
                serviceCall.log(LogLevel.WARNING, String.format("I entered a state I have no action for: %s", newState));
        }
    }
}
