/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceBuilder;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

@Component(name = UtilitiesDeviceCreateRequestCallHandler.NAME, service = ServiceCallHandler.class,
        property = "name=" + UtilitiesDeviceCreateRequestCallHandler.NAME, immediate = true)
public class UtilitiesDeviceCreateRequestCallHandler implements ServiceCallHandler {

    public static final String NAME = "UtilitiesDeviceCreateRequestCallHandler";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

    private volatile SAPCustomPropertySets sapCustomPropertySets;
    private volatile DeviceService deviceService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile Thesaurus thesaurus;

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(WebServiceActivator.COMPONENT_NAME, Layer.SERVICE);
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
        Device device;
        UtilitiesDeviceCreateRequestDomainExtension extension = serviceCall.getExtensionFor(new UtilitiesDeviceCreateRequestCustomPropertySet()).get();
        String serialId = extension.getSerialId();
        String sapDeviceId = extension.getDeviceId();
        List<Device> devices = deviceService.findDevicesBySerialNumber(serialId);
        try {
            if (!devices.isEmpty()) {
                if (devices.size() == 1) {
                    device = devices.get(0);
                } else {
                    extension.setError(MessageSeeds.SEVERAL_DEVICES, serialId);
                    serviceCall.update(extension);
                    serviceCall.requestTransition(DefaultState.FAILED);
                    return;
                }
            } else {
                device = createDevice(extension);
            }

            sapCustomPropertySets.setSapDeviceId(device, sapDeviceId);

            serviceCall.requestTransition(DefaultState.SUCCESSFUL);

        } catch (LocalizedException ex) {
            extension.setError(ex.getMessageSeed(), ex.getMessageArgs());
            serviceCall.update(extension);
            serviceCall.requestTransition(DefaultState.FAILED);
            return;
        }

    }

    private Device createDevice(UtilitiesDeviceCreateRequestDomainExtension extension) {
        DeviceConfiguration deviceConfig = findDeviceConfiguration(extension.getDeviceType());
        DeviceBuilder deviceBuilder = deviceService.newDeviceBuilder(deviceConfig,
                extension.getSerialId(), extension.getShipmentDate());
        deviceBuilder.withSerialNumber(extension.getSerialId());
        deviceBuilder.withManufacturer(extension.getManufacturer());
        deviceBuilder.withModelNumber(extension.getModelNumber());
        return deviceBuilder.create();
    }

    private DeviceConfiguration findDeviceConfiguration(String deviceType) {
        DeviceConfiguration deviceConfiguration =
                deviceConfigurationService.findDeviceTypeByName(deviceType)
                        .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.NO_DEVICE_TYPE_FOUND, deviceType))
                        .getConfigurations()
                        .stream()
                        .filter(config -> config.isDefault())
                        .findAny().orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.NO_DEFAULT_DEVICE_CONFIGURATION, deviceType));
        return deviceConfiguration;
    }
}
