/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;

import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.RetrySearchDataSourceDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation.UtilitiesDeviceRegisterCreateConfirmationMessage;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.AbstractMasterRetryServiceCallHandler;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallHelper;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component(name = MasterUtilitiesDeviceRegisterCreateRequestCallHandler.NAME, service = ServiceCallHandler.class,
        property = "name=" + MasterUtilitiesDeviceRegisterCreateRequestCallHandler.NAME, immediate = true)
public class MasterUtilitiesDeviceRegisterCreateRequestCallHandler extends AbstractMasterRetryServiceCallHandler {

    public static final String NAME = "MasterUtilitiesDeviceRegisterCreateRequestCallHandler";

    private volatile Clock clock;
    private volatile SAPCustomPropertySets sapCustomPropertySets;

    //For OSGI
    public MasterUtilitiesDeviceRegisterCreateRequestCallHandler() {
    }

    @Inject
    public MasterUtilitiesDeviceRegisterCreateRequestCallHandler(ServiceCallService serviceCallService, WebServiceActivator webServiceActivator,
                                                                 Clock clock, SAPCustomPropertySets sapCustomPropertySets) {
        this();
        setServiceCallService(serviceCallService);
        setWebServiceActivator(webServiceActivator);
        setClock(clock);
        setSAPCustomPropertySets(sapCustomPropertySets);
    }

    @Override
    protected RetrySearchDataSourceDomainExtension getMasterDomainExtension(ServiceCall serviceCall) {
        return serviceCall
                .getExtension(MasterUtilitiesDeviceRegisterCreateRequestDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
    }

    @Override
    protected void sendResultMessage(ServiceCall serviceCall) {
        MasterUtilitiesDeviceRegisterCreateRequestDomainExtension extension = serviceCall.getExtensionFor(new MasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet()).get();
        List<ServiceCall> children = ServiceCallHelper.findChildren(serviceCall);
        Instant now = clock.instant();
        UtilitiesDeviceRegisterCreateConfirmationMessage resultMessage = UtilitiesDeviceRegisterCreateConfirmationMessage
                .builder()
                .from(serviceCall, children, webServiceActivator.getMeteringSystemId(), now, extension.isBulk())
                .build();
        if (extension.isBulk()) {
            WebServiceActivator.UTILITIES_DEVICE_REGISTER_BULK_CREATE_CONFIRMATION.forEach(sender -> sender.call(resultMessage));
        } else {
            WebServiceActivator.UTILITIES_DEVICE_REGISTER_CREATE_CONFIRMATION.forEach(sender -> sender.call(resultMessage));
        }


        //check device active and send registered notification
        try {
            List<String> deviceIds = findIdsOfActiveDevicesWithLRN(children, now);
            if (!deviceIds.isEmpty()) {
                if (extension.isBulk()) {
                    WebServiceActivator.UTILITIES_DEVICE_REGISTERED_BULK_NOTIFICATION.forEach(sender -> sender.call(deviceIds));
                } else {
                    WebServiceActivator.UTILITIES_DEVICE_REGISTERED_NOTIFICATION.forEach(sender -> sender.call(deviceIds.get(0)));
                }
            }
        } catch (Exception ex) {
            //If we could not send registered notification due to any exception, we should continue to process service call
            serviceCall.log(LogLevel.WARNING, "Exception while sending registered (bulk) notification: " + ex.getLocalizedMessage());
        }
    }

    private List<String> findIdsOfActiveDevicesWithLRN(List<ServiceCall> children, Instant now) {
        List<String> deviceIds = new ArrayList<>();
        children.forEach(child -> {
            SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension extension = child.getExtensionFor(new SubMasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet()).get();
            String deviceId = extension.getDeviceId();
            Optional<Device> device = sapCustomPropertySets.getDevice(deviceId);
            if (device.isPresent() &&
                    !sapCustomPropertySets.isRegistered(device.get()) &&
                    (child.getState() == DefaultState.SUCCESSFUL || ServiceCallHelper.hasAnyChildState(ServiceCallHelper.findChildren(child), DefaultState.SUCCESSFUL)) &&
                    sapCustomPropertySets.isAnyLrnPresent(device.get().getId(), now)) {
                deviceIds.add(deviceId);
            }
        });

        return deviceIds;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        this.webServiceActivator = webServiceActivator;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }
}
