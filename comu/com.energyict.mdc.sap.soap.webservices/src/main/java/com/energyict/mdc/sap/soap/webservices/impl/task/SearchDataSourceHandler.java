/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.task;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.energyict.mdc.sap.soap.webservices.impl.RetrySearchDataSourceDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterPodNotificationDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceLocationNotificationDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceRegisterCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MasterMeterReadingDocumentCreateRequestDomainExtension;

import java.math.BigDecimal;

public class SearchDataSourceHandler implements TaskExecutor {

    private final ServiceCallService serviceCallService;
    private final WebServiceActivator webServiceActivator;

    public SearchDataSourceHandler(ServiceCallService serviceCallService, WebServiceActivator webServiceActivator) {
        this.serviceCallService = serviceCallService;
        this.webServiceActivator = webServiceActivator;
    }

    @Override
    public void execute(TaskOccurrence taskOccurrence) {
        findAvailableServiceCalls(ServiceCallTypes.MASTER_METER_READING_DOCUMENT_CREATE_REQUEST)
                .stream()
                .forEach(serviceCall -> {
                    serviceCall = lock(serviceCall);
                    MasterMeterReadingDocumentCreateRequestDomainExtension domainExtension = serviceCall.getExtension(MasterMeterReadingDocumentCreateRequestDomainExtension.class).get();
                    nextAttempt(domainExtension);
                });

        findAvailableServiceCalls(ServiceCallTypes.MASTER_UTILITIES_DEVICE_REGISTER_CREATE_REQUEST)
                .stream()
                .forEach(serviceCall -> {
                    serviceCall = lock(serviceCall);
                    MasterUtilitiesDeviceRegisterCreateRequestDomainExtension domainExtension = serviceCall.getExtension(MasterUtilitiesDeviceRegisterCreateRequestDomainExtension.class).get();
                    nextAttempt(domainExtension);
                });

        findAvailableServiceCalls(ServiceCallTypes.MASTER_UTILITIES_DEVICE_LOCATION_NOTIFICATION)
                .stream()
                .forEach(serviceCall -> {
                    serviceCall = lock(serviceCall);
                    MasterUtilitiesDeviceLocationNotificationDomainExtension domainExtension = serviceCall.getExtension(MasterUtilitiesDeviceLocationNotificationDomainExtension.class).get();
                    nextAttempt(domainExtension);
                });

        findAvailableServiceCalls(ServiceCallTypes.MASTER_POD_NOTIFICATION)
                .stream()
                .forEach(serviceCall -> {
                    serviceCall = lock(serviceCall);
                    MasterPodNotificationDomainExtension domainExtension = serviceCall.getExtension(MasterPodNotificationDomainExtension.class).get();
                    nextAttempt(domainExtension);
                });
    }

    private void nextAttempt(RetrySearchDataSourceDomainExtension domainExtension) {
        ServiceCall serviceCall = domainExtension.getServiceCall();
        BigDecimal currentAttempt = domainExtension.getAttemptNumber();

        domainExtension.setAttemptNumber(currentAttempt.add(BigDecimal.ONE));
        serviceCall.update(domainExtension);
        switch (serviceCall.getState()) {
            case SCHEDULED:
                serviceCall.requestTransition(DefaultState.PENDING);
                break;
            case PAUSED:
                serviceCall.requestTransition(DefaultState.ONGOING);
                break;
        }
    }

    private Finder<ServiceCall> findAvailableServiceCalls(ServiceCallTypes serviceCallType) {
        ServiceCallFilter filter = new ServiceCallFilter();
        filter.types.add(serviceCallType.getTypeName());
        filter.states.add(DefaultState.SCHEDULED.name());
        filter.states.add(DefaultState.PAUSED.name());
        return serviceCallService.getServiceCallFinder(filter);
    }

    private ServiceCall lock(ServiceCall serviceCall) {
        return serviceCallService.lockServiceCall(serviceCall.getId())
                .orElseThrow(() -> new IllegalStateException("Service call " + serviceCall.getNumber() + " disappeared."));
    }
}