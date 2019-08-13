/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.measurementtaskassignment;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointProp;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MeasurementTaskAssignmentChangeFactory;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.TranslationKeys;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangerequest.UtilitiesTimeSeriesERPMeasurementTaskAssignmentChangeRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangerequest.UtilsTmeSersERPMsmtTskAssgmtChgReqMsg;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MeasurementTaskAssignmentChangeRequestEndpoint extends AbstractInboundEndPoint implements UtilitiesTimeSeriesERPMeasurementTaskAssignmentChangeRequestCIn, EndPointProp, ApplicationSpecific {

    private final Clock clock;
    private final DataExportService dataExportService;
    private volatile MeasurementTaskAssignmentChangeFactory measurementTaskAssignmentChangeFactory;
    private final PropertySpecService propertySpecService;
    private final SAPCustomPropertySets sapCustomPropertySets;
    private final Thesaurus thesaurus;

    @Inject
    MeasurementTaskAssignmentChangeRequestEndpoint(Clock clock,
                                                   DataExportService dataExportService,
                                                   MeasurementTaskAssignmentChangeFactory measurementTaskAssignmentChangeFactory,
                                                   PropertySpecService propertySpecService,
                                                   SAPCustomPropertySets sapCustomPropertySets,
                                                   Thesaurus thesaurus) {
        this.clock = clock;
        this.dataExportService = dataExportService;
        this.measurementTaskAssignmentChangeFactory = measurementTaskAssignmentChangeFactory;
        this.propertySpecService = propertySpecService;
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.thesaurus = thesaurus;
    }

    @Override
    public void utilitiesTimeSeriesERPMeasurementTaskAssignmentChangeRequestCIn(UtilsTmeSersERPMsmtTskAssgmtChgReqMsg request) {
        runInTransactionWithOccurrence(() -> {
            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> handleMessage(requestMessage));
            return null;
        });
    }

    public void handleMessage(UtilsTmeSersERPMsmtTskAssgmtChgReqMsg msg) {

        MeasurementTaskAssignmentChangeRequestMessage message = MeasurementTaskAssignmentChangeRequestMessage.builder().from(msg).build();
        if (!message.isValidId()) {
            sendProcessError(message, MessageSeeds.INVALID_MESSAGE_FORMAT);
            return;
        }

        if (!message.isPeriodsValid()) {
            sendProcessError(message, MessageSeeds.INVALID_TIME_PERIOD);
            return;
        }

        if (sapCustomPropertySets.isRangesIntersected(message.getRoles().stream()
                .map(r -> Range.closedOpen(r.getStartDateTime(), r.getEndDateTime())).collect(Collectors.toList()))) {
            sendProcessError(message, MessageSeeds.TIME_PERIODS_INTERSECT);
            return;
        }

        try {
            measurementTaskAssignmentChangeFactory.processServiceCall(message);
            // send successful response
            MeasurementTaskAssignmentChangeConfirmationMessage confirmationMessage =
                    MeasurementTaskAssignmentChangeConfirmationMessage.builder(clock.instant(), message.getId())
                            .from()
                            .build();

            sendMessage(confirmationMessage);
        } catch (Exception ex) {
            if (ex instanceof SAPWebServiceException) {
                MessageSeed messageSeed = ((SAPWebServiceException) ex).getMessageSeed();
                String errorMessage = ex.getLocalizedMessage();
                log(LogLevel.SEVERE, thesaurus.getFormat(messageSeed).format(((SAPWebServiceException) ex).getMessageArgs()));
                MeasurementTaskAssignmentChangeConfirmationMessage confirmationMessage =
                        MeasurementTaskAssignmentChangeConfirmationMessage.builder(clock.instant(), message.getId())
                                .from(messageSeed.getLevel().getName(), String.valueOf(messageSeed.getNumber()), errorMessage)
                                .build();
                sendMessage(confirmationMessage);
            } else {
                MessageSeeds messageSeeds = MessageSeeds.EXCEPTION_GENERATED;
                String errorMessage = messageSeeds.translate(thesaurus, ex.getLocalizedMessage());
                log(LogLevel.SEVERE, thesaurus.getFormat(messageSeeds).format(ex.getLocalizedMessage()));
                MeasurementTaskAssignmentChangeConfirmationMessage confirmationMessage =
                        MeasurementTaskAssignmentChangeConfirmationMessage.builder(clock.instant(), message.getId())
                                .from(messageSeeds.getLevel().getName(), messageSeeds.code(), errorMessage)
                                .build();
                sendMessage(confirmationMessage);
            }
            throw ex;
        }
    }

    private void sendMessage(MeasurementTaskAssignmentChangeConfirmationMessage confirmationMessage) {
        WebServiceActivator.MEASUREMENT_TASK_ASSIGNMENT_CHANGE_CONFIRMATIONS
                .forEach(service -> service.call(confirmationMessage));
    }

    private void sendProcessError(MeasurementTaskAssignmentChangeRequestMessage message, MessageSeeds messageSeed, Object... args) {
        MeasurementTaskAssignmentChangeConfirmationMessage confirmationMessage =
                MeasurementTaskAssignmentChangeConfirmationMessage.builder(clock.instant(), message.getId())
                        .from(messageSeed.getLevel().getName(), messageSeed.code(), messageSeed.translate(thesaurus, args))
                        .build();
        sendMessage(confirmationMessage);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();

        if (dataExportService.getAvailableSelectors().stream().filter(s -> s.getName().equals(DataExportService.CUSTOM_READINGTYPE_DATA_SELECTOR)).findAny().isPresent()) {
            builder.add(propertySpecService
                    .booleanSpec()
                    .named(TranslationKeys.EXPORTER)
                    .describedAs(TranslationKeys.EXPORTER)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .markEditable()
                    .finish());
        }
        return builder.build();
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}
