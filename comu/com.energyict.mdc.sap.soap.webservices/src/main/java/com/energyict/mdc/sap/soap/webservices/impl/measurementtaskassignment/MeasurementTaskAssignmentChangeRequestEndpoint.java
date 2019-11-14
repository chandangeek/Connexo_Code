/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.measurementtaskassignment;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.SelectorType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointProp;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.MeasurementTaskAssignmentChangeProcessor;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangerequest.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangerequest.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangerequest.UUID;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangerequest.UtilitiesTimeSeriesERPMeasurementTaskAssignmentChangeRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangerequest.UtilsTmeSersERPMsmtTskAssgmtChgReqMsg;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.energyict.mdc.sap.soap.webservices.impl.TranslationKeys.EXPORTER;
import static com.energyict.mdc.sap.soap.webservices.impl.TranslationKeys.EXPORTER_DESCRIPTION;

public class MeasurementTaskAssignmentChangeRequestEndpoint extends AbstractInboundEndPoint implements UtilitiesTimeSeriesERPMeasurementTaskAssignmentChangeRequestCIn, EndPointProp, ApplicationSpecific {

    private final Clock clock;
    private final DataExportService dataExportService;
    private final MeasurementTaskAssignmentChangeProcessor measurementTaskAssignmentChangeProcessor;
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    @Inject
    MeasurementTaskAssignmentChangeRequestEndpoint(Clock clock,
                                                   DataExportService dataExportService,
                                                   MeasurementTaskAssignmentChangeProcessor measurementTaskAssignmentChangeProcessor,
                                                   PropertySpecService propertySpecService,
                                                   Thesaurus thesaurus) {
        this.clock = clock;
        this.dataExportService = dataExportService;
        this.measurementTaskAssignmentChangeProcessor = measurementTaskAssignmentChangeProcessor;
        this.propertySpecService = propertySpecService;
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
        String id = getId(msg);
        String uuid = getUuid(msg);
        try {
            MeasurementTaskAssignmentChangeRequestMessage message = MeasurementTaskAssignmentChangeRequestMessage.builder(thesaurus).from(msg, id, uuid).build();

            SetMultimap<String, String> values = HashMultimap.create();
            values.put(SapAttributeNames.SAP_UTILITIES_TIME_SERIES_ID.getAttributeName(),
                    message.getProfileId());
            message.getRoles().forEach(role ->
                    values.put(SapAttributeNames.SAP_UTILITIES_MEASUREMENT_TASK_ID.getAttributeName(),
                            role.getLrn()));

            saveRelatedAttributes(values);
            if (id == null && uuid == null) {
                throw new SAPWebServiceException(thesaurus, MessageSeeds.INVALID_MESSAGE_FORMAT);
            }

            if (!message.arePeriodsValid()) {
                throw new SAPWebServiceException(thesaurus, MessageSeeds.INVALID_TIME_PERIOD);
            }

            if (Ranges.doAnyRangesIntersect(message.getRoles().stream()
                    .filter(role -> !WebServiceActivator.getListOfRoleCodes().contains(role.getRoleCode()))
                    .map(r -> Range.closedOpen(r.getStartDateTime(), r.getEndDateTime())).collect(Collectors.toList()))) {
                throw new SAPWebServiceException(thesaurus, MessageSeeds.TIME_PERIODS_INTERSECT);
            }

            Optional<String> selectorName = Optional.ofNullable((String) getEndPointConfiguration().getPropertiesWithValue().get(EXPORTER.getKey()));
            measurementTaskAssignmentChangeProcessor.process(message, selectorName.isPresent() ? selectorName.get() :
                    dataExportService.getAvailableSelectors().stream()
                            .filter(s -> s.getName().equals(DataExportService.STANDARD_READINGTYPE_DATA_SELECTOR))
                            .findAny().get().getDisplayName());
            // send successful response
            MeasurementTaskAssignmentChangeConfirmationMessage confirmationMessage =
                    MeasurementTaskAssignmentChangeConfirmationMessage.builder(clock.instant(), id, uuid)
                            .create()
                            .build();
            sendMessage(confirmationMessage);
        } catch (SAPWebServiceException e) {
            MessageSeed messageSeed = e.getMessageSeed();
            String errorMessage = e.getLocalizedMessage();
            log(LogLevel.SEVERE, thesaurus.getFormat(messageSeed).format(e.getMessageArgs()));
            MeasurementTaskAssignmentChangeConfirmationMessage confirmationMessage =
                    MeasurementTaskAssignmentChangeConfirmationMessage.builder(clock.instant(), id, uuid)
                            .from(messageSeed.getLevel().getName(), errorMessage)
                            .build();
            sendMessage(confirmationMessage);
            throw e;
        } catch (Exception e) {
            MessageSeeds messageSeeds = MessageSeeds.ERROR_PROCESSING_MTA_REQUEST;
            String errorMessage = messageSeeds.translate(thesaurus, e.getLocalizedMessage());
            log(LogLevel.SEVERE, thesaurus.getFormat(messageSeeds).format(e.getLocalizedMessage()));
            MeasurementTaskAssignmentChangeConfirmationMessage confirmationMessage =
                    MeasurementTaskAssignmentChangeConfirmationMessage.builder(clock.instant(), id, uuid)
                            .from(messageSeeds.getLevel().getName(), errorMessage)
                            .build();
            sendMessage(confirmationMessage);
            throw e;
        }
    }

    private String getId(UtilsTmeSersERPMsmtTskAssgmtChgReqMsg msg) {
        Optional<BusinessDocumentMessageHeader> header = Optional.ofNullable(msg.getMessageHeader());
        if (header.isPresent()) {
            return Optional.ofNullable(header.get().getID())
                    .map(BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }
        return null;
    }

    private String getUuid(UtilsTmeSersERPMsmtTskAssgmtChgReqMsg msg) {
        Optional<BusinessDocumentMessageHeader> header = Optional.ofNullable(msg.getMessageHeader());
        if (header.isPresent()) {
            return Optional.ofNullable(header.get().getUUID())
                    .map(UUID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }
        return null;
    }

    private void sendMessage(MeasurementTaskAssignmentChangeConfirmationMessage confirmationMessage) {
        WebServiceActivator.MEASUREMENT_TASK_ASSIGNMENT_CHANGE_CONFIRMATIONS
                .forEach(service -> service.call(confirmationMessage));
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();

        List<String> selectors = dataExportService.getAvailableSelectors()
                .stream()
                .filter(s -> s.getSelectorType().equals(SelectorType.DEFAULT_READINGS)).map(s -> s.getDisplayName())
                .collect(Collectors.toList());

        if (selectors.size() > 1) {
            Optional<String> defaultValue = dataExportService.getAvailableSelectors()
                    .stream()
                    .filter(s -> s.getSelectorType().equals(SelectorType.DEFAULT_READINGS))
                    .filter(s -> s.isDefault()).map(s -> s.getDisplayName()).findFirst();
            builder.add(propertySpecService
                    .specForValuesOf(new StringFactory())
                    .named(EXPORTER)
                    .describedAs(EXPORTER_DESCRIPTION)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .addValues(selectors)
                    .setDefaultValue(defaultValue.isPresent() ? defaultValue.get() : null)
                    .markExhaustive(PropertySelectionMode.COMBOBOX)
                    .finish());
        }
        return builder.build();
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}
