/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.servicecall.examples;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;

/**
 * Example handler taking care of a setting the year of certification of a device
 * You can test a controlled failure by using a BC year or non-existing device
 */
@Component(name = "com.energyict.mdc.servicecall.example.device.certification.handler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=DeviceCertificationHandler")
public class DeviceCertificationHandler implements ServiceCallHandler {

    private volatile DeviceService deviceService;

    public DeviceCertificationHandler() {
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Activate
    public void activate() {
        System.out.println("Activating device certification handler");
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINEST, "Now entering state " + newState.getKey());
        switch (newState) {
            case ONGOING:
                updateYear(serviceCall);
                break;
            case PENDING:
                serviceCall.requestTransition(DefaultState.ONGOING);
                break;
            default:
                serviceCall.log(LogLevel.WARNING, String.format("I entered a state I have no action for: %s", newState));
        }
    }

    protected void updateYear(ServiceCall serviceCall) {
        DeviceCertificationDomainExtension extensionFor = serviceCall.getExtensionFor(new DeviceCertificationCustomPropertySet())
                .get();
        Optional<Device> device = deviceService.findDeviceById(extensionFor.getDeviceId());
        if (device.isPresent()) {
            serviceCall.log(LogLevel.FINE, "Device found");
            int yearOfCertification = (int) extensionFor.getYearOfCertification();
            if (yearOfCertification < 0) {
                serviceCall.log(LogLevel.SEVERE, "The romans did not have smart meters. Well, neither do Belgians of course. Invalid year: " + yearOfCertification);
                serviceCall.requestTransition(DefaultState.FAILED);
                return;
            }
            device.get().setYearOfCertification(yearOfCertification);
            serviceCall.log(LogLevel.FINE, "Year updated");
            device.get().save();
            serviceCall.log(LogLevel.INFO, "Device updated");
            serviceCall.requestTransition(DefaultState.SUCCESSFUL);
        } else {
            serviceCall.log(LogLevel.SEVERE, "No device with id " + extensionFor.getDeviceId());
            serviceCall.requestTransition(DefaultState.FAILED);
        }
    }
}
