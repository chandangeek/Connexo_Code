/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;

import com.energyict.mdc.sap.soap.webservices.SAPMeterReadingDocumentReason;
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
    public static final String APPLICATION = "MultiSense";

    private volatile Clock clock;
    private volatile MeteringService meteringService;

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case PENDING:
                serviceCall.requestTransition(DefaultState.ONGOING);
                break;
            case ONGOING:
                executeReasonCodeProvider(serviceCall);
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
            serviceCall.requestTransition(DefaultState.PAUSED);
        } else if (domainExtension.getChannelId() == null) {
            serviceCall.requestTransition(DefaultState.FAILED);
        } else {
            Optional.of(domainExtension)
                    .map(MeterReadingDocumentCreateResultDomainExtension::getReadingReasonCode)
                    .map(this::findReadingReasonProvider)
                    .map(Optional::get)
                    .orElseThrow(() -> new IllegalStateException("Unable to get reading reason provider for service call"))
                    .process(SAPMeterReadingDocumentCollectionDataBuilder.builder(meteringService)
                            .from(serviceCall, WebServiceActivator.SAP_PROPERTIES)
                            .build());
        }
    }

    private Optional<SAPMeterReadingDocumentReason> findReadingReasonProvider(String readingReasonCode) {
        return WebServiceActivator.METER_READING_REASONS
                .stream()
                .filter(readingReason -> readingReason.getCode().equals(readingReasonCode))
                .findFirst();
    }
}