/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.AdditionalProperties;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Component(name = "MeterReadingDocumentCreateRequestServiceCallHandler",
        service = ServiceCallHandler.class, immediate = true,
        property = "name=" + MeterReadingDocumentCreateRequestServiceCallHandler.NAME)
public class MeterReadingDocumentCreateRequestServiceCallHandler implements ServiceCallHandler {

    public static final String NAME = "MeterReadingDocumentCreateRequestServiceCallHandler";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

    private volatile Clock clock;
    private volatile SAPCustomPropertySets sapCustomPropertySets;
    private volatile ServiceCallService serviceCallService;

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case PENDING:
                serviceCall.transitionWithLockIfPossible(DefaultState.ONGOING);
                break;
            case ONGOING:
                serviceCallService.lockServiceCall(serviceCall.getId()).ifPresent(lockedServiceCall ->{
                    if(lockedServiceCall.getState().equals(DefaultState.ONGOING)) {
                        processServiceCall(lockedServiceCall);
                    }
                });

                break;
            case CANCELLED:
                    MeterReadingDocumentCreateRequestDomainExtension extension = serviceCall
                            .getExtension(MeterReadingDocumentCreateRequestDomainExtension.class)
                            .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
                    if(extension.getCancelledBySap() == null) {
                        extension.setCancelledBySap(false);
                        serviceCall.update(extension);
                    }
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    private void processServiceCall(ServiceCall serviceCall) {
        MeterReadingDocumentCreateRequestDomainExtension extension = serviceCall.getExtensionFor(new MeterReadingDocumentCreateRequestCustomPropertySet()).get();
        Optional<Device> device = sapCustomPropertySets.getDevice(extension.getDeviceId());
        if (device.isPresent() && device.get().getStage().getName().equals(EndDeviceStage.OPERATIONAL.getKey())) {
            extension.setDeviceName(device.get().getName());
            Optional<Channel> channel = sapCustomPropertySets.getChannel(extension.getLrn(), extension.getScheduledReadingDate());
            if (channel.isPresent()) {
                extension.setChannelId(new BigDecimal(channel.get().getId()));
                channel.get().getReadingTypes()
                        .stream()
                        .filter(ReadingType::isCumulative)
                        .findFirst()
                        .ifPresent(readingType -> extension.setDataSource(readingType.getMRID()));
            } else {
                serviceCall.log(LogLevel.WARNING,"The channel/register isn't found.");
                serviceCall.update(extension);
                serviceCall.requestTransition(DefaultState.PAUSED);
                return;
            }

            WebServiceActivator.findReadingReasonProvider(extension.getReadingReasonCode()).ifPresent(provider -> {
                Instant plannedReadingCollectionDate = provider.hasCollectionInterval()
                        ? extension.getScheduledReadingDate().plusSeconds(WebServiceActivator.SAP_PROPERTIES
                        .get(AdditionalProperties.READING_COLLECTION_INTERVAL) * 60)
                        : extension.getScheduledReadingDate();

                boolean futureCase = clock.instant().isBefore(plannedReadingCollectionDate);
                extension.setFutureCase(futureCase);
                if (futureCase) {
                    extension.setProcessingDate(plannedReadingCollectionDate);
                }
            });

            serviceCall.update(extension);
            serviceCall.requestTransition(DefaultState.SUCCESSFUL);
        } else {
            serviceCall.log(LogLevel.WARNING,"The device isn't found or the device is not in operational stage.");
            serviceCall.requestTransition(DefaultState.PAUSED);
        }
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
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }
}

