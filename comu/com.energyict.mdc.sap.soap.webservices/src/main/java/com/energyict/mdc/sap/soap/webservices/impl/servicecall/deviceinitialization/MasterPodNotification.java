/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization;

import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;

import com.energyict.mdc.sap.soap.webservices.impl.RetrySearchDataSourceDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.AbstractMasterRetryServiceCallHandler;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = MasterPodNotification.NAME, service = ServiceCallHandler.class,
        property = "name=" + MasterPodNotification.NAME, immediate = true)
public class MasterPodNotification extends AbstractMasterRetryServiceCallHandler {
    public static final String NAME = "MasterPodNotification";

    //For OSGI
    public MasterPodNotification() {
    }

    @Inject
    public MasterPodNotification(ServiceCallService serviceCallService, WebServiceActivator webServiceActivator) {
        this();
        setServiceCallService(serviceCallService);
        setWebServiceActivator(webServiceActivator);
    }

    @Override
    protected RetrySearchDataSourceDomainExtension getMasterDomainExtension(ServiceCall serviceCall) {
        return serviceCall
                .getExtension(MasterPodNotificationDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        this.webServiceActivator = webServiceActivator;
    }
}
