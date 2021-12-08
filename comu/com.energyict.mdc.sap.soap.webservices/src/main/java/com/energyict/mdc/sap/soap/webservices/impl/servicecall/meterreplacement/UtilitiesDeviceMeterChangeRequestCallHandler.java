/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.common.device.data.CIMLifecycleDates;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component(name = UtilitiesDeviceMeterChangeRequestCallHandler.NAME, service = ServiceCallHandler.class,
        property = "name=" + UtilitiesDeviceMeterChangeRequestCallHandler.NAME, immediate = true)
public class UtilitiesDeviceMeterChangeRequestCallHandler implements ServiceCallHandler {

    public static final String NAME = "UtilitiesDeviceMeterChangeRequestCallHandler";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

    private volatile SAPCustomPropertySets sapCustomPropertySets;
    private volatile DeviceService deviceService;
    private volatile Thesaurus thesaurus;
    private DeviceLifeCycleService deviceLifeCycleService;

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(WebServiceActivator.COMPONENT_NAME, Layer.SERVICE);
    }

    @Reference
    public void setDeviceLifeCycleService(DeviceLifeCycleService deviceLifeCycleService) { this.deviceLifeCycleService = deviceLifeCycleService; }

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
            default:
                // No specific action required for these states
                break;
        }
    }

    private void processServiceCall(ServiceCall serviceCall) {
        UtilitiesDeviceMeterChangeRequestDomainExtension extension = serviceCall.getExtensionFor(new UtilitiesDeviceMeterChangeRequestCustomPropertySet()).get();

        try {
            processDeviceChanging(extension);
            serviceCall.requestTransition(DefaultState.SUCCESSFUL);
        } catch (Exception ex) {
            if (ex instanceof LocalizedException) {
                extension.setError(((LocalizedException) ex).getMessageSeed(), ((LocalizedException) ex).getMessageArgs());
            } else {
                extension.setError(MessageSeeds.ERROR_PROCESSING_METER_CHANGE_REQUEST, ex.getLocalizedMessage());
                serviceCall.log(MessageFormat.format(MessageSeeds.ERROR_PROCESSING_METER_CHANGE_REQUEST.getDefaultFormat(),
                        ex.getLocalizedMessage()), ex);
            }

            serviceCall.update(extension);
            serviceCall.requestTransition(DefaultState.FAILED);
        }
    }

    private void processDeviceChanging(UtilitiesDeviceMeterChangeRequestDomainExtension extension) {
        String sapDeviceId = extension.getDeviceId();
        Device device = getDevice(extension);
        Optional<Device> sapDevice = sapCustomPropertySets.getDevice(sapDeviceId);
        if (sapDevice.isPresent()) {
            if (device.equals(sapCustomPropertySets.getDevice(sapDeviceId).get())) {
                sapCustomPropertySets.setActivationGroupAMIFunctions(device, extension.getActivationGroupAmiFunctions());
                sapCustomPropertySets.setSmartMeterFunctionGroup(device, extension.getMeterFunctionGroup());
                sapCustomPropertySets.setAttributeMessage(device, extension.getAttributeMessage());
                sapCustomPropertySets.setCharacteristicsId(device, extension.getCharacteristicsId());
                sapCustomPropertySets.setCharacteristicsValue(device, extension.getCharacteristicsValue());


                CIMLifecycleDates lifecycleDates = device.getLifecycleDates();
                setShipmentDate(device, extension, lifecycleDates);
                setDeactivationDate(device, extension, lifecycleDates);

            } else {
                throw new SAPWebServiceException(thesaurus, MessageSeeds.DEVICE_MISMATCH);
            }
        } else {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_DEVICE_FOUND_BY_SAP_ID, sapDeviceId);
        }

    }

    private Device getDevice(UtilitiesDeviceMeterChangeRequestDomainExtension extension) {
        String name = extension.getSerialId();

        String deviceTypeName = extension.getDeviceType();
        Optional<Device> device = deviceService.findDeviceByName(name);
        if (device.isPresent()) {
            if (device.get().getDeviceConfiguration().getDeviceType().getName().equals(deviceTypeName)) {
                return deviceService.findAndLockDeviceByNameAndVersion(name, device.get().getVersion()).get();
            } else {
                throw new SAPWebServiceException(thesaurus, MessageSeeds.OTHER_DEVICE_TYPE, extension.getDeviceType());
            }
        } else {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_DEVICE_FOUND_BY_NAME);
        }
    }

    private void setShipmentDate(Device device, UtilitiesDeviceMeterChangeRequestDomainExtension extension, CIMLifecycleDates lifecycleDates) {
        Optional<Instant> installationDate = device.getLifecycleDates().getInstalledDate();
        if (extension.getShipmentDate() != null && States.SHIPMENT_DATE.isEditableForState(device.getState())) {
            if (installationDate.isPresent()) {
                if (extension.getShipmentDate().isBefore(installationDate.get())) {
                    lifecycleDates.setReceivedDate(extension.getShipmentDate());
                    lifecycleDates.save();
                }
            } else {
                lifecycleDates.setReceivedDate(extension.getShipmentDate());
                lifecycleDates.save();
            }
        }
    }

    private void setDeactivationDate(Device device, UtilitiesDeviceMeterChangeRequestDomainExtension extension, CIMLifecycleDates lifecycleDates) {
        if (extension.getShipmentDate() != null && States.DEACTIVATION_DATE.isEditableForState(device.getState())) {
            Optional<ExecutableAction> action = deviceLifeCycleService.getExecutableActions(device)
                    .stream()
                    .filter(candidate -> ((AuthorizedTransitionAction) candidate.getAction()).getStateTransition().getTo().getName().equals(com.elster.jupiter.metering.DefaultState.INACTIVE.getKey()))
                    .findFirst();

            action.get().execute(Instant.now().plusSeconds(3600 * 2), null);

            lifecycleDates.setReceivedDate(extension.getShipmentDate());
            lifecycleDates.save();
        }
    }

    private void validateShipmentDate(Device device, Instant startDate) {
        CIMLifecycleDates lifecycleDates = device.getLifecycleDates();
        Instant shipmentDate = lifecycleDates.getReceivedDate().orElseGet(device::getCreateTime);
        if (startDate.isBefore(shipmentDate)) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.START_DATE_IS_BEFORE_SHIPMENT_DATE);
        }
    }

    public enum States {
        SHIPMENT_DATE {
            @Override
            public List<com.elster.jupiter.metering.DefaultState> attributeIsEditableForStates() {
                return Collections.singletonList(com.elster.jupiter.metering.DefaultState.IN_STOCK);
            }
        },
        INSTALLATION_DATE {
            @Override
            public List<com.elster.jupiter.metering.DefaultState> attributeIsEditableForStates() {
                return Arrays.asList(
                        com.elster.jupiter.metering.DefaultState.COMMISSIONING,
                        com.elster.jupiter.metering.DefaultState.ACTIVE,
                        com.elster.jupiter.metering.DefaultState.INACTIVE
                );
            }
        },
        DEACTIVATION_DATE {
            @Override
            public List<com.elster.jupiter.metering.DefaultState> attributeIsEditableForStates() {
                return Arrays.asList(com.elster.jupiter.metering.DefaultState.ACTIVE,
                        com.elster.jupiter.metering.DefaultState.IN_STOCK,
                        com.elster.jupiter.metering.DefaultState.COMMISSIONING);

            }
        },
        DECOMMISSIONING_DATE {
            @Override
            public List<com.elster.jupiter.metering.DefaultState> attributeIsEditableForStates() {
                return Collections.singletonList(com.elster.jupiter.metering.DefaultState.DECOMMISSIONED);
            }

        };

        public List<com.elster.jupiter.metering.DefaultState> attributeIsEditableForStates() {
            return Arrays.asList(
                    com.elster.jupiter.metering.DefaultState.IN_STOCK,
                    com.elster.jupiter.metering.DefaultState.COMMISSIONING,
                    com.elster.jupiter.metering.DefaultState.ACTIVE,
                    com.elster.jupiter.metering.DefaultState.INACTIVE
            );
        }

        public boolean isEditableForState(State state) {
            if (this.attributeIsEditableForStates() != null) {
                Optional<com.elster.jupiter.metering.DefaultState> correspondingDefaultState = com.elster.jupiter.metering.DefaultState.from(state);
                if (correspondingDefaultState.isPresent()) {
                    return correspondingDefaultState.isPresent()
                            && this.attributeIsEditableForStates().contains(correspondingDefaultState.get());
                }
                return true;
            }
            return false;
        }
    }


    }
