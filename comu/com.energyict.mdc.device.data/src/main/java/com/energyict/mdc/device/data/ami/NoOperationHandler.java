package com.energyict.mdc.device.data.ami;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * Dummy implementation of {@link ServiceCallHandler} interface which doesn't do a thing.
 *
 * @author sva
 * @since 31/03/16 - 13:05
 */
@Component(name = "com.com.energyict.mdc.metering.multisense.no.operation.handler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=MultiSenseNoOperationHandler")
public class NoOperationHandler implements ServiceCallHandler {

    public NoOperationHandler() {
    }

    @Activate
    public void activate() {
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        //TODO: implement CANCEL operation (e.g.: cancel all not yet executed DeviceCommands)
    }

    @Override
    public boolean allowStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        return true; // TODO: implement for CANCEL operation
    }

    @Override
    public void onChildStateChange(ServiceCall parent, ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
    }
}
