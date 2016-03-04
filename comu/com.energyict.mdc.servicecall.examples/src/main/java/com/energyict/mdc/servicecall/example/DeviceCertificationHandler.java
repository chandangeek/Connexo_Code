package com.energyict.mdc.servicecall.example;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * Example handler taking care of a setting the year of certification of a device
 */
@Component(name = "com.energyict.mdc.servicecall.example.device.certification.handler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=DeviceCertificationHandler")
public class DeviceCertificationHandler implements ServiceCallHandler {

    public DeviceCertificationHandler() {
    }

    @Activate
    public void activate() {
        System.out.println("Activating Device certification handler");
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        System.out.println("Now entering state " + newState.getKey());
        for (RegisteredCustomPropertySet propertySet : serviceCall.getType().getCustomPropertySets()) {
            serviceCall.getExtensionFor(propertySet);
        }

    }
}
