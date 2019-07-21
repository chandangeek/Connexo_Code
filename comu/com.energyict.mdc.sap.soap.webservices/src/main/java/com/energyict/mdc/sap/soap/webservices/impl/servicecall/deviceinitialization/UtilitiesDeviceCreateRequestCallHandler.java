/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

@Component(name = UtilitiesDeviceCreateRequestCallHandler.NAME, service = ServiceCallHandler.class,
        property = "name=" + UtilitiesDeviceCreateRequestCallHandler.NAME, immediate = true)
public class UtilitiesDeviceCreateRequestCallHandler implements ServiceCallHandler {

    public static final String NAME = "UtilitiesDeviceCreateRequestCallHandler";
    public static final String VERSION = "v1.0";

    private volatile SAPCustomPropertySets sapCustomPropertySets;
    private volatile DeviceService deviceService;

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case PENDING:
                serviceCall.requestTransition(DefaultState.ONGOING);
                break;
            case ONGOING:
                processServiceCall(serviceCall);
                break;
            case CANCELLED:
                cancelServiceCall(serviceCall);
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    private void cancelServiceCall(ServiceCall serviceCall) {
        UtilitiesDeviceCreateRequestDomainExtension extension = serviceCall.getExtensionFor(new UtilitiesDeviceCreateRequestCustomPropertySet()).get();
        extension.setError(MessageSeeds.SERVICE_CALL_WAS_CANCELLED);
        serviceCall.update(extension);
    }

    private void processServiceCall(ServiceCall serviceCall) {
        UtilitiesDeviceCreateRequestDomainExtension extension = serviceCall.getExtensionFor(new UtilitiesDeviceCreateRequestCustomPropertySet()).get();
        String serialId = extension.getSerialId();
        List<Device> devices = deviceService.findDevicesBySerialNumber(serialId);
        if (!devices.isEmpty()) {
            if (devices.size() == 1) {
                Device device = devices.get(0);
                String sapDeviceId = extension.getDeviceId();

                try {
                    sapCustomPropertySets.addSapDeviceId(device, sapDeviceId);
                } catch (SAPWebServiceException ex) {
                    extension.setError(ex.getMessageSeed(),ex.getMessageArgs());
                    serviceCall.update(extension);
                    serviceCall.requestTransition(DefaultState.FAILED);
                    return;
                }
                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
            } else {
                extension.setError(MessageSeeds.SEVERAL_DEVICES, serialId);
                serviceCall.update(extension);
                serviceCall.requestTransition(DefaultState.FAILED);
            }
        } else {
            extension.setError(MessageSeeds.NO_DEVICE_FOUND_BY_SERIAL_ID, serialId);
            serviceCall.update(extension);
            serviceCall.requestTransition(DefaultState.FAILED);
        }
    }
}
