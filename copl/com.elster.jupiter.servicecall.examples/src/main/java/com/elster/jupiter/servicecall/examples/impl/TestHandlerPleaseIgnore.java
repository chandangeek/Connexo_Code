package com.elster.jupiter.servicecall.examples.impl;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

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
        if (DefaultState.WAITING.equals(newState)) {
            throw new RuntimeException("Unhandled exception when entering state " + newState.name());
        } else {
            LOGGER.info("Entering state " + newState.name());
        }
    }

    @Override
    public void onChildStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        if (DefaultState.WAITING.equals(newState)) {
            throw new RuntimeException("Unhandled exception when child " + serviceCall.getNumber() + " entering state " + newState.name());
        } else {
            LOGGER.info("Child "  + serviceCall.getNumber() + " entering state " + newState.name());
        }
    }
}
