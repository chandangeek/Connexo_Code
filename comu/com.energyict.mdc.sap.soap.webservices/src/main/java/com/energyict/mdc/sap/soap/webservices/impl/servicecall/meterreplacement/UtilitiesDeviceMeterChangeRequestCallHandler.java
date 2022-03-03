/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
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
import com.energyict.mdc.sap.soap.webservices.SapDeviceInfo;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.States;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Optional;

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
        thesaurus = nlsService.getThesaurus(WebServiceActivator.COMPONENT_NAME, Layer.SOAP);
    }

    @Reference
    public void setDeviceLifeCycleService(DeviceLifeCycleService deviceLifeCycleService) {
        this.deviceLifeCycleService = deviceLifeCycleService;
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
        Optional<Device> device = sapCustomPropertySets.getDevice(sapDeviceId);
        if (device.isPresent()) {
            if (device.get().getName().equals(extension.getSerialId())) {
                if (device.get().getDeviceType().getName().equals(extension.getDeviceType())) {

                    CIMLifecycleDates lifecycleDates = device.get().getLifecycleDates();
                    if (extension.getShipmentDate() != null) {
                        setShipmentDate(device.get(), extension, lifecycleDates);
                    }
                    if (extension.getDeactivationDate() != null) {
                        setDeactivationDate(device.get(), extension, lifecycleDates);
                    }

                    SapDeviceInfo sapDeviceInfo = sapCustomPropertySets.findSapDeviceInfo(device.get()).orElseGet(() -> sapCustomPropertySets.newSapDeviceInfoInstance(device.get()));

                    if (extension.getActivationGroupAmiFunctions() != null && !extension.getActivationGroupAmiFunctions().isEmpty()) {
                        sapDeviceInfo.setActivationGroupAmiFunctions(extension.getActivationGroupAmiFunctions());
                    }
                    if (extension.getMeterFunctionGroup() != null && !extension.getMeterFunctionGroup().isEmpty()) {
                        sapDeviceInfo.setMeterFunctionGroup(extension.getMeterFunctionGroup());
                    }
                    if (extension.getAttributeMessage() != null && !extension.getAttributeMessage().isEmpty()) {
                        sapDeviceInfo.setAttributeMessage(extension.getAttributeMessage());
                    }
                    if (extension.getCharacteristicsId() != null && !extension.getCharacteristicsId().isEmpty()) {
                        sapDeviceInfo.setCharacteristicsId(extension.getCharacteristicsId());
                    }
                    if (extension.getCharacteristicsValue() != null && !extension.getCharacteristicsValue().isEmpty()) {
                        sapDeviceInfo.setCharacteristicsValue(extension.getCharacteristicsValue());
                    }
                    sapDeviceInfo.save();
                } else {
                    throw new SAPWebServiceException(thesaurus, MessageSeeds.OTHER_DEVICE_TYPE, extension.getDeviceType());
                }
            } else {
                throw new SAPWebServiceException(thesaurus, MessageSeeds.DEVICE_MISMATCH);
            }
        } else {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_DEVICE_FOUND_BY_SAP_ID, sapDeviceId);
        }

    }


    private void setShipmentDate(Device device, UtilitiesDeviceMeterChangeRequestDomainExtension extension, CIMLifecycleDates lifecycleDates) {
        Optional<Instant> installationDate = device.getLifecycleDates().getInstalledDate();
        if (extension.getShipmentDate() != null) {
            if (States.SHIPMENT_DATE.isEditableForState(device.getState())) {
                if (installationDate.isPresent()) {
                    if (extension.getShipmentDate().isBefore(installationDate.get())) {
                        lifecycleDates.setReceivedDate(extension.getShipmentDate());
                        lifecycleDates.save();
                    } else {
                        throw new SAPWebServiceException(thesaurus, MessageSeeds.SHIPMENT_DATE_IS_AFTER_INSTALLATION_DATE);
                    }
                } else {
                    lifecycleDates.setReceivedDate(extension.getShipmentDate());
                    lifecycleDates.save();
                }
            } else {
                throw new SAPWebServiceException(thesaurus, MessageSeeds.SHIPMENT_DATE_IS_NOT_EDITABLE);
            }
        }
    }

    private void setDeactivationDate(Device device, UtilitiesDeviceMeterChangeRequestDomainExtension extension, CIMLifecycleDates lifecycleDates) {
        LocalDate date = extension.getDeactivationDate().atZone(ZoneId.systemDefault()).toLocalDate();
        if (extension.getDeactivationDate() != null && date.getYear() != 9999) {
            if (States.DEACTIVATION_DATE.isEditableForState(device.getState())) {
                Optional<ExecutableAction> action = deviceLifeCycleService.getExecutableActions(device)
                        .stream()
                        .filter(candidate -> ((AuthorizedTransitionAction) candidate.getAction()).getStateTransition()
                                .getTo()
                                .getName()
                                .equals(com.elster.jupiter.metering.DefaultState.INACTIVE.getKey()))
                        .findFirst();
                if (action.isPresent()) {

                    action.get().execute(extension.getDeactivationDate(), Collections.emptyList());
                } else {
                    throw new SAPWebServiceException(thesaurus, MessageSeeds.TRANSITION_IS_NOT_FOUND);
                }
            } else {
                throw new SAPWebServiceException(thesaurus, MessageSeeds.DEACTIVATION_DATE_IS_NOT_EDITABLE);
            }
        }
    }

}
