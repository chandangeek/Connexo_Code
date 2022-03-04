/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
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

@Component(name = PodNotificationServiceCallHandler.NAME, service = ServiceCallHandler.class,
        property = "name=" + PodNotificationServiceCallHandler.NAME, immediate = true)
public class PodNotificationServiceCallHandler extends AbstractChildRetryServiceCallHandler {
    public static final String NAME = "PodNotification";

    //For OSGI
    public PodNotificationServiceCallHandler() {
    }

    @Inject
    public PodNotificationServiceCallHandler(SAPCustomPropertySets sapCustomPropertySets, WebServiceActivator webServiceActivator) {
        this();
        setSAPCustomPropertySets(sapCustomPropertySets);
        setWebServiceActivator(webServiceActivator);
    }

    @Override
    protected RetrySearchDataSourceDomainExtension getMasterDomainExtension(ServiceCall serviceCall) {
        return serviceCall.getParent().get()
                .getExtension(MasterPodNotificationDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
    }

    @Override
    protected void processServiceCall(ServiceCall serviceCall) {
        try {
            processPodNotification(serviceCall);
        } catch (LocalizedException localizedEx) {
            failServiceCall(serviceCall, localizedEx.getMessageSeed(), localizedEx.getMessageArgs());
        } catch (Exception ex) {
            failServiceCallWithException(serviceCall, ex, MessageSeeds.ERROR_PROCESSING_METER_POD_NOTIFICATION, ex.getLocalizedMessage());
        }
    }

    private void processPodNotification(ServiceCall serviceCall) {
        PodNotificationDomainExtension extension = serviceCall
                .getExtensionFor(new PodNotificationCustomPropertySet()).get();
        Optional<Device> device = sapCustomPropertySets.getDevice(extension.getDeviceId());
        if (device.isPresent()) {
            serviceCall.setTargetObject(device.get());
            serviceCall.save();
            sapCustomPropertySets.setPod(device.get(), extension.getPodId());
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
