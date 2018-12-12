/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.examples.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.util.Optional;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.servicecall.example.test.handler.please.ignore",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=TEST")
public class TestHandlerPleaseIgnore implements ServiceCallHandler {

    private static final Logger LOGGER = Logger.getLogger(TestHandlerPleaseIgnore.class.getName());

    @Activate
    public void activate() {
        LOGGER.info("Activating TestHandlerPleaseIgnore");
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        switch (newState) {
            case WAITING:
                throw new RuntimeException("Unhandled exception when entering state " + newState.name());
            default:
                LOGGER.info("Entering state " + newState.name());
                serviceCall.log(LogLevel.INFO, "Entering state " + newState.name());

                Optional<CustomPropertySet> propertySet = serviceCall.getType().getCustomPropertySets().stream()
                        .map(cps -> cps.getCustomPropertySet())
                        .filter(cps -> cps.getName().equals("ServiceCallOneCustomPropertySet"))
                        .findFirst();

                if (propertySet.isPresent()) {
                    ServiceCallDomainExtension extension = (ServiceCallDomainExtension) serviceCall.getExtensionFor(propertySet.get())
                            .orElse(new ServiceCallDomainExtension());

                    extension.setTestString(newState.getDefaultFormat());
                    serviceCall.update(extension);
                    break;
                } else {
                    break;
                }
        }
    }

    @Override
    public void onChildStateChange(ServiceCall parent, ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        switch (newState) {
            case WAITING:
                throw new RuntimeException("Unhandled exception when child " + serviceCall.getNumber() + " entering state " + newState.name());
            default:
                LOGGER.info("Child " + serviceCall.getNumber() + " entering state " + newState.name());
                serviceCall.log(LogLevel.INFO, "Child " + serviceCall.getNumber() + " entering state " + newState.name());
        }
    }
}
