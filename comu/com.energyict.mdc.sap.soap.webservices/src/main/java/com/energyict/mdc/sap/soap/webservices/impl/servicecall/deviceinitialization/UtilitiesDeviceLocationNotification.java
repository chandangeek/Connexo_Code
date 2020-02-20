/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.util.exception.MessageSeed;

import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.RetrySearchDataSourceDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.AbstractChildRetryServiceCallHandler;

import com.google.inject.Inject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;

@Component(name = UtilitiesDeviceLocationNotification.NAME, service = ServiceCallHandler.class,
        property = "name=" + UtilitiesDeviceLocationNotification.NAME, immediate = true)
public class UtilitiesDeviceLocationNotification extends AbstractChildRetryServiceCallHandler {
    public static final String NAME = "UtilitiesDeviceLocationNotification";

    //For OSGI
    public UtilitiesDeviceLocationNotification() {
    }

    @Inject
    public UtilitiesDeviceLocationNotification(SAPCustomPropertySets sapCustomPropertySets, WebServiceActivator webServiceActivator) {
        this();
        setSAPCustomPropertySets(sapCustomPropertySets);
        setWebServiceActivator(webServiceActivator);
    }

    @Override
    protected RetrySearchDataSourceDomainExtension getMasterDomainExtension(ServiceCall serviceCall) {
        return serviceCall.getParent().get()
                .getExtension(MasterUtilitiesDeviceLocationNotificationDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
    }

    protected void processServiceCall(ServiceCall serviceCall) {
        try {
            processLocationNotification(serviceCall);
        } catch (LocalizedException localizedEx) {
            failServiceCall(serviceCall, localizedEx.getMessageSeed(), localizedEx.getMessageArgs());
        } catch (Exception ex) {
            failServiceCall(serviceCall, MessageSeeds.ERROR_PROCESSING_METER_LOCATION_NOTIFICATION, ex.getLocalizedMessage());
        }
    }

    @Override
    protected void cancelServiceCall(ServiceCall serviceCall) {
        //no action
    }

    @Override
    protected void setError(ServiceCall serviceCall, MessageSeed error, Object... args) {
        //no action
    }

    private void processLocationNotification(ServiceCall serviceCall) {
        UtilitiesDeviceLocationNotificationDomainExtension extension = serviceCall
                .getExtensionFor(new UtilitiesDeviceLocationNotificationCustomPropertySet()).get();
        Optional<Device> device = sapCustomPropertySets.getDevice(extension.getDeviceId());
        if (device.isPresent()) {
            serviceCall.setTargetObject(device.get());
            serviceCall.save();
            sapCustomPropertySets.setLocation(device.get(), extension.getLocationId());
            serviceCall.requestTransition(DefaultState.SUCCESSFUL);
        } else {
            failedAttempt(serviceCall, MessageSeeds.NO_DEVICE_FOUND_BY_SAP_ID, extension.getDeviceId());
        }
    }

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        this.webServiceActivator = webServiceActivator;
    }
}
