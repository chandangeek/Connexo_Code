package com.elster.jupiter.demo.impl.amiscsexample;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * Implementation of {@link ServiceCallHandler} interface which contains only logging functionality.
 * On state changes, no actual operations will be executed/launched, only thing done is logging a message regarding the state change.
 *
 * @author sva
 * @since 31/03/16 - 13:05
 */
@Component(name = "com.elster.jupiter.demo.impl.amiscsexample.mutisense.logging.handler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=MultiSenseLoggingHandler")
public class LoggingHandler implements ServiceCallHandler {

    public LoggingHandler() {
    }

    @Activate
    public void activate() {
        System.out.println("Activating Multisense Logging Handler");
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.INFO, "Now entering state " + newState.getDefaultFormat());
    }

    @Override
    public void onChildStateChange(ServiceCall parent, ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.INFO, "Child " + serviceCall.getNumber() + " entering state " + newState.getKey());
    }
}
