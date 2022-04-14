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
import com.energyict.mdc.sap.soap.webservices.SapDeviceInfo;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.RetrySearchDataSourceDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.AbstractChildRetryServiceCallHandler;

import com.google.inject.Inject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;

@Component(name = UtilitiesDeviceLocationNotificationServiceCallHandler.NAME, service = ServiceCallHandler.class,
        property = "name=" + UtilitiesDeviceLocationNotificationServiceCallHandler.NAME, immediate = true)
public class UtilitiesDeviceLocationNotificationServiceCallHandler extends AbstractChildRetryServiceCallHandler {
    public static final String NAME = "UtilitiesDeviceLocationNotification";

    //For OSGI
    public UtilitiesDeviceLocationNotificationServiceCallHandler() {
    }

    @Inject
    public UtilitiesDeviceLocationNotificationServiceCallHandler(SAPCustomPropertySets sapCustomPropertySets, WebServiceActivator webServiceActivator) {
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
            failServiceCallWithException(serviceCall, ex, MessageSeeds.ERROR_PROCESSING_METER_LOCATION_NOTIFICATION, ex.getLocalizedMessage());
        }
    }

    private void processLocationNotification(ServiceCall serviceCall) {
        UtilitiesDeviceLocationNotificationDomainExtension extension = serviceCall
                .getExtensionFor(new UtilitiesDeviceLocationNotificationCustomPropertySet()).get();
        Optional<Device> device = sapCustomPropertySets.getDevice(extension.getDeviceId());
        if (device.isPresent()) {
            serviceCall.setTargetObject(device.get());
            serviceCall.save();
            SapDeviceInfo sapDeviceInfo = sapCustomPropertySets.findSapDeviceInfo(device.get()).orElseGet(() -> sapCustomPropertySets.newSapDeviceInfoInstance(device.get()));
            sapDeviceInfo.setDeviceLocation(extension.getLocationId());
            if (extension.getInstallationNumber() != null && !extension.getInstallationNumber().isEmpty()) {
                sapDeviceInfo.setInstallationNumber(extension.getInstallationNumber());
            }
            if (extension.getPod() != null && !extension.getPod().isEmpty()) {
                sapDeviceInfo.setPointOfDelivery(extension.getPod());
            }
            if (extension.getDivisionCategoryCode() != null && !extension.getDivisionCategoryCode().isEmpty()) {
                sapDeviceInfo.setDivisionCategoryCode(extension.getDivisionCategoryCode());
            }
            sapDeviceInfo.setDeviceLocationInformation(extension.getLocationInformation());
            sapDeviceInfo.setModificationInformation(extension.getModificationInformation());
            sapDeviceInfo.save();
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
