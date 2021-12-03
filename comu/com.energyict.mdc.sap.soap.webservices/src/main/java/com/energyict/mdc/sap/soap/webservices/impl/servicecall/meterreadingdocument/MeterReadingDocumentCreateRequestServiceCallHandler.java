/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.SAPMeterReadingDocumentReason;
import com.energyict.mdc.sap.soap.webservices.impl.AdditionalProperties;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SapMeterReadingDocumentReasonProviderHelper;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.text.MessageFormat;
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
    private volatile WebServiceActivator webServiceActivator;

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case PENDING:
                serviceCall.transitionWithLockIfPossible(DefaultState.ONGOING);
                break;
            case ONGOING:
                serviceCallService.lockServiceCall(serviceCall.getId()).ifPresent(lockedServiceCall -> {
                    if (lockedServiceCall.getState().equals(DefaultState.ONGOING)) {
                        processServiceCall(lockedServiceCall);
                    }
                });

                break;
            case CANCELLED:
                MeterReadingDocumentCreateRequestDomainExtension extension = serviceCall
                        .getExtension(MeterReadingDocumentCreateRequestDomainExtension.class)
                        .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
                if (extension.getCancelledBySap() == null) {
                    extension.setCancelledBySap(false);
                    extension.setErrorMessage(MessageSeeds.REQUEST_CANCELLED_MANUALLY);
                } else {
                    extension.setErrorMessage(MessageSeeds.REQUEST_CANCELLED_BY_SAP);
                }
                serviceCall.update(extension);
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    private void processServiceCall(ServiceCall serviceCall) {
        MeterReadingDocumentCreateRequestDomainExtension extension = serviceCall.getExtensionFor(new MeterReadingDocumentCreateRequestCustomPropertySet()).get();
        Optional<SAPMeterReadingDocumentReason> readingReasonProvider = SapMeterReadingDocumentReasonProviderHelper.findReadingReasonProvider(extension.getReadingReasonCode(),extension.getDataSourceTypeCode());
        if (!readingReasonProvider.isPresent()) {
            extension.setErrorMessage(SapMeterReadingDocumentReasonProviderHelper.getErrorMessage());
            serviceCall.update(extension);
            serviceCall.requestTransition(DefaultState.FAILED);
            return;
        }

        Optional<Device> device = sapCustomPropertySets.getDevice(extension.getDeviceId());
        Optional<Channel> channel;
        Optional<? extends ReadingType> dataSource;
        if (device.isPresent()) {
            extension.setDeviceName(device.get().getName());
            channel = sapCustomPropertySets.getChannel(extension.getLrn(), extension.getScheduledReadingDate());
            if (channel.isPresent()) {
                extension.setChannelId(new BigDecimal(channel.get().getId()));
                dataSource = channel.get().getReadingTypes()
                        .stream()
                        .reduce((a, b) -> b); // we need the collected reading type from the channel, it should be the last
                if (dataSource.isPresent()) {
                    extension.setDataSource(dataSource.get().getMRID());
                } else {
                    failedAttempt(extension, MessageSeeds.READING_TYPE_IS_NOT_FOUND);
                    return;
                }
            } else {
                failedAttempt(extension, MessageSeeds.CHANNEL_REGISTER_IS_NOT_FOUND);
                return;
            }

            if (!readingReasonProvider.get().validateComTaskExecutionIfNeeded(device.get(), channel.get().isRegular(), dataSource.get())) {
                extension.setErrorMessage(MessageSeeds.COM_TASK_COULD_NOT_BE_LOCATED);
                serviceCall.update(extension);
                serviceCall.requestTransition(DefaultState.FAILED);
                return;
            }

            Instant plannedReadingCollectionDate = readingReasonProvider.get().hasCollectionInterval()
                    ? extension.getScheduledReadingDate().plusSeconds(webServiceActivator.getSapProperty(AdditionalProperties.READING_COLLECTION_INTERVAL) * 60)
                    : extension.getScheduledReadingDate();

            boolean futureCase = clock.instant().isBefore(plannedReadingCollectionDate);
            extension.setFutureCase(futureCase);
            if (futureCase) {
                extension.setProcessingDate(plannedReadingCollectionDate);
            }

            if (!channel.get().isRegular()) {
                Optional<Pair<String, String>> dataSourceInterval = readingReasonProvider.get().getExtraDataSourceMacroAndMeasuringCodes();
                if (dataSourceInterval.isPresent() && !dataSourceInterval.get().equals(Pair.of("0", "0"))) {
                    String extraDataSource = dataSourceInterval.get().getFirst()
                            + extension.getDataSource().substring(1, 4)
                            + dataSourceInterval.get().getLast()
                            + extension.getDataSource().substring(5);

                    device.get().getChannels().stream().filter(c -> c.getReadingType().getMRID().equals(extraDataSource))
                            .findFirst()
                            .ifPresent(readingType -> extension.setExtraDataSource(extraDataSource));
                }
            }

            serviceCall.update(extension);
            serviceCall.requestTransition(DefaultState.SUCCESSFUL);
        } else {
            failedAttempt(extension, MessageSeeds.DEVICE_IS_NOT_FOUND);
        }
    }

    private void failedAttempt(MeterReadingDocumentCreateRequestDomainExtension extension, MessageSeeds error) {
        ServiceCall serviceCall = extension.getServiceCall();

        MasterMeterReadingDocumentCreateRequestDomainExtension masterExtension = serviceCall.getParent().get()
                .getExtension(MasterMeterReadingDocumentCreateRequestDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
        BigDecimal attempts = new BigDecimal(webServiceActivator.getSapProperty(AdditionalProperties.OBJECT_SEARCH_ATTEMPTS));
        BigDecimal currentAttempt = masterExtension.getAttemptNumber();
        if (currentAttempt.compareTo(attempts) >= 0) {
            serviceCall.log(LogLevel.SEVERE, MessageFormat.format(error.getDefaultFormat(), new Object[0]));
            extension.setErrorMessage(error);
            serviceCall.update(extension);
            serviceCall.requestTransition(DefaultState.FAILED);
        } else {
            serviceCall.log(LogLevel.WARNING, MessageFormat.format(error.getDefaultFormat(), new Object[0]));
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

    @Reference
    public final void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        this.webServiceActivator = webServiceActivator;
    }
}