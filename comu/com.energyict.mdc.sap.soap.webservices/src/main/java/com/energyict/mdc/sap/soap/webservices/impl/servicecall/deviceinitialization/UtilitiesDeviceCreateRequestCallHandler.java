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

import java.util.Optional;

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
        extension.setError(MessageSeeds.REQUEST_CANCELLED);
        serviceCall.update(extension);
    }

    private void processServiceCall(ServiceCall serviceCall) {
        UtilitiesDeviceCreateRequestDomainExtension extension = serviceCall.getExtensionFor(new UtilitiesDeviceCreateRequestCustomPropertySet()).get();

        try {
            processDeviceCreation(extension);
            serviceCall.requestTransition(DefaultState.SUCCESSFUL);
        } catch (Exception ex) {
            if(ex instanceof LocalizedException){
                extension.setError(((LocalizedException)ex).getMessageSeed(), ((LocalizedException)ex).getMessageArgs());
            }else{
                extension.setError(MessageSeeds.ERROR_PROCESSING_METER_CREATE_REQUEST, ex.getLocalizedMessage());
            }

            serviceCall.update(extension);
            serviceCall.requestTransition(DefaultState.FAILED);
        }
    }

    private void processDeviceCreation(UtilitiesDeviceCreateRequestDomainExtension extension) {
        String sapDeviceId = extension.getDeviceId();
        String serialId = extension.getSerialId();

        validateSapDeviceIdUniqueness(sapDeviceId, serialId);
        Device device = getOrCreateDevice(extension);
        sapCustomPropertySets.setSapDeviceId(device, sapDeviceId);
    }

    private void validateSapDeviceIdUniqueness(String sapDeviceId, String serialId) {
        Optional<Device> other = sapCustomPropertySets.getDevice(sapDeviceId);
        if (other.isPresent() && !other.get().getSerialNumber().equals(serialId)) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.SAP_DEVICE_IDENTIFIER_MUST_BE_UNIQUE);
        }
    }

    private Device getOrCreateDevice(UtilitiesDeviceCreateRequestDomainExtension extension) {
        //name = serial id for SAP
        String name = extension.getSerialId();
        return deviceService.findDeviceByName(name)
                .orElseGet(() -> createDevice(extension));
    }

    private Device createDevice(UtilitiesDeviceCreateRequestDomainExtension extension) {
        String deviceTypeName = extension.getDeviceType();
        if (deviceTypeName == null) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.DEVICE_TYPE_IS_NOT_MAPPED, extension.getMaterialId());
        }

        DeviceConfiguration deviceConfig = findDeviceConfiguration(deviceTypeName);
        DeviceBuilder deviceBuilder = deviceService.newDeviceBuilder(deviceConfig,
                extension.getSerialId(), extension.getShipmentDate());
        deviceBuilder.withSerialNumber(extension.getSerialId());
        deviceBuilder.withManufacturer(extension.getManufacturer());
        deviceBuilder.withModelNumber(extension.getModelNumber());
        Device device = deviceBuilder.create();
        ServiceCall serviceCall = extension.getServiceCall();
        serviceCall.setTargetObject(device);
        serviceCall.save();
        return device;
    }

    private DeviceConfiguration findDeviceConfiguration(String deviceTypeName) {
        DeviceConfiguration deviceConfiguration =
                deviceConfigurationService.findDeviceTypeByName(deviceTypeName)
                        .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.NO_DEVICE_TYPE_FOUND, deviceTypeName))
                        .getConfigurations()
                        .stream()
                        .filter(config -> config.isDefault())
                        .findAny().orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.NO_DEFAULT_DEVICE_CONFIGURATION, deviceTypeName));
        return deviceConfiguration;
    }

}
