/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.uploadusagedata;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.webservicecall.DataExportServiceCallType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.InboundSoapEndPointProvider;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangeconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangeconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangeconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangeconfirmation.UUID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangeconfirmation.UtilitiesTimeSeriesERPItemBulkChangeConfirmationCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangeconfirmation.UtilitiesTimeSeriesID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangeconfirmation.UtilsTmeSersERPItmBulkChgConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangeconfirmation.UtilsTmeSersERPItmChgConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangeconfirmation.UtilsTmeSersERPItmChgConfUtilsTmeSers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "com.energyict.mdc.sap.soap.webservices.impl.uploadusagedata.UtilitiesTimeSeriesBulkChangeConfirmationReceiver",
        service = {InboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + UtilitiesTimeSeriesBulkChangeConfirmationReceiver.NAME})
public class UtilitiesTimeSeriesBulkChangeConfirmationReceiver extends AbstractInboundEndPoint implements InboundSoapEndPointProvider, UtilitiesTimeSeriesERPItemBulkChangeConfirmationCIn, ApplicationSpecific {
    static final String NAME = "SAP TimeSeriesBulkChangeConfirmation";
    private static final Set<String> FAILURE_CODES = ImmutableSet.of(ProcessingResultCode.FAILED.getCode());

    private volatile DataExportServiceCallType dataExportServiceCallType;
    private volatile Thesaurus thesaurus;

    public UtilitiesTimeSeriesBulkChangeConfirmationReceiver() {
        // for OSGi purposes
    }

    @Inject
    public UtilitiesTimeSeriesBulkChangeConfirmationReceiver(DataExportService dataExportService, Thesaurus thesaurus) {
        setDataExportService(dataExportService);
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setDataExportService(DataExportService dataExportService) {
        this.dataExportServiceCallType = dataExportService.getDataExportServiceCallType();
    }

    @Reference
    public void setTranslationsProvider(WebServiceActivator translationsProvider) {
        this.thesaurus = translationsProvider.getThesaurus();
    }

    @Override
    public UtilitiesTimeSeriesBulkChangeConfirmationReceiver get() {
        return this;
    }

    @Override
    public void utilitiesTimeSeriesERPItemBulkChangeConfirmationCIn(UtilsTmeSersERPItmBulkChgConfMsg confirmation) {
        runInTransactionWithOccurrence(() -> {
            SetMultimap<String, String> values = HashMultimap.create();
            getChangeConfirmationMessages(confirmation).stream()
                    .map(UtilitiesTimeSeriesBulkChangeConfirmationReceiver::getProfileId)
                    .flatMap(Functions.asStream())
                    .forEach(value -> values.put(SapAttributeNames.SAP_UTILITIES_TIME_SERIES_ID.getAttributeName(), value));
            saveRelatedAttributes(values);
            Optional<String> uuid = findReferenceUuid(confirmation);
            ServiceCall serviceCall = uuid.flatMap(dataExportServiceCallType::findServiceCall)
                    .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.UNEXPECTED_CONFIRMATION_MESSAGE, uuid.orElse("null")));
            switch (getResultCode(confirmation)) {
                case SUCCESSFUL:
                    dataExportServiceCallType.tryPassingServiceCall(serviceCall);
                    break;
                case PARTIALLY_SUCCESSFUL:
                    List<String> successfulProfileIds = getSuccessfulProfileIds(confirmation);
                    dataExportServiceCallType.tryPartialPassingServiceCallByProfileIds(serviceCall, successfulProfileIds, getSeverestError(confirmation).orElse(null));
                    break;
                case FAILED:
                    dataExportServiceCallType.tryFailingServiceCall(serviceCall, getSeverestError(confirmation).orElse(null));
                    break;
            }
            return null;
        });
    }

    private static List<String> getSuccessfulProfileIds(UtilsTmeSersERPItmBulkChgConfMsg confirmation) {
        return Optional.ofNullable(confirmation)
                .map(UtilsTmeSersERPItmBulkChgConfMsg::getUtilitiesTimeSeriesERPItemChangeConfirmationMessage)
                .map(List::stream)
                .orElseGet(Stream::empty)
                .filter(UtilitiesTimeSeriesBulkChangeConfirmationReceiver::isChildConfirmed)
                .map(UtilsTmeSersERPItmChgConfMsg::getUtilitiesTimeSeries)
                .map(UtilsTmeSersERPItmChgConfUtilsTmeSers::getID)
                .map(UtilitiesTimeSeriesID::getValue)
                .collect(Collectors.toList());
    }

    private static boolean isChildConfirmed(UtilsTmeSersERPItmChgConfMsg item) {
        return Optional.ofNullable(item)
                .map(UtilsTmeSersERPItmChgConfMsg::getLog)
                .map(Log::getBusinessDocumentProcessingResultCode)
                .filter(code->code.equals(ProcessingResultCode.SUCCESSFUL.getCode()))
                .isPresent();
    }

    private static List<UtilsTmeSersERPItmChgConfMsg> getChangeConfirmationMessages(UtilsTmeSersERPItmBulkChgConfMsg confirmation) {
        return Optional.ofNullable(confirmation)
                .map(UtilsTmeSersERPItmBulkChgConfMsg::getUtilitiesTimeSeriesERPItemChangeConfirmationMessage)
                .orElse(Collections.emptyList());
    }

    private static Optional<String> getProfileId(UtilsTmeSersERPItmChgConfMsg message) {
        return Optional.ofNullable(message)
                .map(UtilsTmeSersERPItmChgConfMsg::getUtilitiesTimeSeries)
                .map(UtilsTmeSersERPItmChgConfUtilsTmeSers::getID)
                .map(UtilitiesTimeSeriesID::getValue);
    }

    private static Optional<String> findReferenceUuid(UtilsTmeSersERPItmBulkChgConfMsg confirmation) {
        return Optional.ofNullable(confirmation)
                .map(UtilsTmeSersERPItmBulkChgConfMsg::getMessageHeader)
                .flatMap(UtilitiesTimeSeriesBulkChangeConfirmationReceiver::findReferenceUuid);
    }

    private static Optional<String> findReferenceUuid(BusinessDocumentMessageHeader header) {
        return Optional.ofNullable(header.getReferenceUUID()).map(UUID::getValue);
    }

    private static ProcessingResultCode getResultCode(UtilsTmeSersERPItmBulkChgConfMsg confirmation) {
        return Optional.ofNullable(confirmation)
                .map(UtilsTmeSersERPItmBulkChgConfMsg::getLog)
                .map(Log::getBusinessDocumentProcessingResultCode)
                .map(ProcessingResultCode::valueFor)
                .orElse(ProcessingResultCode.FAILED);
    }

    private static Optional<String> getSeverestError(UtilsTmeSersERPItmBulkChgConfMsg confirmation) {
        return Optional.ofNullable(confirmation)
                .map(UtilsTmeSersERPItmBulkChgConfMsg::getLog)
                .map(Log::getItem)
                .map(List::stream)
                .orElseGet(Stream::empty)
                .reduce(UtilitiesTimeSeriesBulkChangeConfirmationReceiver::findMaximumSeverityOrNotNullOrWhatever)
                .map(LogItem::getNote);
    }

    private static LogItem findMaximumSeverityOrNotNullOrWhatever(LogItem item1, LogItem item2) {
        String s1 = item1.getSeverityCode();
        String s2 = item2.getSeverityCode();
        if (s2 == null) {
            return item1;
        } else if (s1 == null) {
            return item2;
        } else {
            s1 = s1.trim();
            s2 = s2.trim();
            int i1, i2;
            try {
                i2 = Integer.parseInt(s2);
            } catch (NumberFormatException e) {
                return item1;
            }
            try {
                i1 = Integer.parseInt(s1);
            } catch (NumberFormatException e) {
                return item2;
            }
            return i1 < i2 ? item2 : i2 < i1 ? item1 : item1.getNote() == null ? item2 : item1;
        }
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}
