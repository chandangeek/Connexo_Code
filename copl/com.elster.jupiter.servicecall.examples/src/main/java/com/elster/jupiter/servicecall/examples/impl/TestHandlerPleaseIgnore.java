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
        LOGGER.info("Entering state " + newState.name());
    }
}
