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
import com.energyict.mdc.common.device.data.CIMLifecycleDates;
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

import java.text.MessageFormat;
import java.time.Instant;
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
            if (ex instanceof LocalizedException) {
                extension.setError(((LocalizedException) ex).getMessageSeed(), ((LocalizedException) ex).getMessageArgs());
            } else {
                extension.setError(MessageSeeds.ERROR_PROCESSING_METER_CREATE_REQUEST, ex.getLocalizedMessage());
                serviceCall.log(MessageFormat.format(MessageSeeds.ERROR_PROCESSING_METER_CREATE_REQUEST.getDefaultFormat(),
                        ex.getLocalizedMessage()), ex);
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
        validateShipmentDate(device, extension.getShipmentDate());
        sapCustomPropertySets.setSapDeviceId(device, sapDeviceId);
        sapCustomPropertySets.setActivationGroupAMIFunctions(device, extension.getActivationGroupAmiFunctions());
        sapCustomPropertySets.setSmartMeterFunctionGroup(device, extension.getMeterFunctionGroup());
        sapCustomPropertySets.setAttributeMessage(device, extension.getAttributeMessage());
        sapCustomPropertySets.setCharacteristicsId(device, extension.getCharacteristicsId());
        sapCustomPropertySets.setCharacteristicsValue(device, extension.getCharacteristicsValue());
    }

    private void validateShipmentDate(Device device, Instant startDate) {
        CIMLifecycleDates lifecycleDates = device.getLifecycleDates();
        Instant shipmentDate = lifecycleDates.getReceivedDate().orElseGet(device::getCreateTime);
        if (startDate.isBefore(shipmentDate)) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.START_DATE_IS_BEFORE_SHIPMENT_DATE);
        }
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

        String deviceTypeName = extension.getDeviceType();
        if (deviceTypeName == null) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.DEVICE_TYPE_IS_NOT_MAPPED, extension.getMaterialId());
        }
        Optional<Device> device = deviceService.findDeviceByName(name);
        if (device.isPresent()) {
            if (device.get().getDeviceConfiguration().getDeviceType().getName().equals(deviceTypeName)) {
                return device.get();
            } else {
                throw new SAPWebServiceException(thesaurus, MessageSeeds.DIFFERENT_DEVICE_TYPE, extension.getMaterialId());
            }
        } else {
            return createDevice(extension, deviceTypeName);
        }
    }

    private Device createDevice(UtilitiesDeviceCreateRequestDomainExtension extension, String deviceTypeName) {
        DeviceConfiguration deviceConfig = findDeviceConfiguration(deviceTypeName);
        DeviceBuilder deviceBuilder = deviceService.newDeviceBuilder(deviceConfig,
                extension.getSerialId(), extension.getShipmentDate());
        deviceBuilder.withSerialNumber(extension.getManufacturerSerialId());
        deviceBuilder.withManufacturer(extension.getManufacturer());
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
