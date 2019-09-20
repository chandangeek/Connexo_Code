/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;

import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.SAPMeterReadingDocumentCollectionDataBuilder;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;
import java.util.Optional;

@Component(name = "MeterReadingDocumentCreateResultServiceCallHandler", service = ServiceCallHandler.class,
        immediate = true, property = "name=" + MeterReadingDocumentCreateResultServiceCallHandler.NAME)
public class MeterReadingDocumentCreateResultServiceCallHandler implements ServiceCallHandler {

    public static final String NAME = "MeterReadingDocumentCreateResultServiceCallHandler";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

    private volatile Clock clock;
    private volatile MeteringService meteringService;
    private volatile ServiceCallService serviceCallService;

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case PENDING:
                serviceCallService.transitionWithLockIfPossible(serviceCall, DefaultState.ONGOING);
                break;
            case ONGOING:
                if (!oldState.equals(DefaultState.WAITING)) {
                    executeReasonCodeProvider(serviceCall);
                }
                break;
            default:
                break;
        }
    }

    private void executeReasonCodeProvider(ServiceCall serviceCall) {
        MeterReadingDocumentCreateResultDomainExtension domainExtension = serviceCall
                .getExtension(MeterReadingDocumentCreateResultDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));

        if (domainExtension.isFutureCase() && domainExtension.getProcessingDate().isAfter(clock.instant())) {
            serviceCallService.transitionWithLockIfPossible(serviceCall, DefaultState.PAUSED);
        } else if (domainExtension.getChannelId() == null) {
            serviceCallService.transitionWithLockIfPossible(serviceCall, DefaultState.WAITING);
        } else {
            Optional.of(domainExtension)
                    .map(MeterReadingDocumentCreateResultDomainExtension::getReadingReasonCode)
                    .map(WebServiceActivator::findReadingReasonProvider)
                    .map(Optional::get)
                    .orElseThrow(() -> new IllegalStateException("Unable to get reading reason provider for service call"))
                    .process(SAPMeterReadingDocumentCollectionDataBuilder.builder(meteringService, clock, serviceCallService)
                            .from(serviceCall, WebServiceActivator.SAP_PROPERTIES)
                            .build());
        }
    }
}