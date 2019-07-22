/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.uploadusagedata;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.webservicecall.DataExportServiceCallType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.InboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangeconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangeconfirmation.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangeconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangeconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangeconfirmation.UUID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangeconfirmation.UtilitiesTimeSeriesERPItemBulkChangeConfirmationEIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangeconfirmation.UtilsTmeSersERPItmBulkChgConfMsg;

import com.google.common.collect.ImmutableSet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Component(name = UtilitiesTimeSeriesBulkChangeConfirmationReceiver.NAME,
        service = {InboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + UtilitiesTimeSeriesBulkChangeConfirmationReceiver.NAME})
public class UtilitiesTimeSeriesBulkChangeConfirmationReceiver extends AbstractInboundEndPoint implements InboundSoapEndPointProvider, UtilitiesTimeSeriesERPItemBulkChangeConfirmationEIn, ApplicationSpecific {
    static final String NAME = "SAP UtilitiesTimeSeriesERPItemBulkChangeConfirmation_C_In";
    private static final Set<String> FAILURE_CODES = ImmutableSet.of("5");

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
    public void utilitiesTimeSeriesERPItemBulkChangeConfirmationEIn(UtilsTmeSersERPItmBulkChgConfMsg confirmation) {
        runInTransactionWithOccurrence(() -> {
            Optional<String> uuid = findReferenceUuid(confirmation);
            ServiceCall serviceCall = uuid.flatMap(dataExportServiceCallType::findServiceCall)
                    .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.UNEXPECTED_CONFIRMATION_MESSAGE, uuid.orElse("null")));
            if (isConfirmed(confirmation)) {
                dataExportServiceCallType.tryPassingServiceCall(serviceCall);
            } else {
                dataExportServiceCallType.tryFailingServiceCall(serviceCall, getSeverestError(confirmation).orElse(null));
            }
            return null;
        });
    }

    private static Optional<String> findReferenceUuid(UtilsTmeSersERPItmBulkChgConfMsg confirmation) {
        return Optional.ofNullable(confirmation)
                .map(UtilsTmeSersERPItmBulkChgConfMsg::getMessageHeader)
                .flatMap(UtilitiesTimeSeriesBulkChangeConfirmationReceiver::findReferenceUuid);
    }

    private static Optional<String> findReferenceUuid(BusinessDocumentMessageHeader header) {
        return Stream.<Supplier<Optional<String>>>of(
                () -> Optional.ofNullable(header.getReferenceID()).map(BusinessDocumentMessageID::getValue),
                () -> Optional.ofNullable(header.getReferenceUUID()).map(UUID::getValue))
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .findFirst()
                .map(Optional::get);
    }

    private static boolean isConfirmed(UtilsTmeSersERPItmBulkChgConfMsg confirmation) {
        return Optional.ofNullable(confirmation)
                .map(UtilsTmeSersERPItmBulkChgConfMsg::getLog)
                .map(Log::getBusinessDocumentProcessingResultCode)
                .filter(Predicates.not(FAILURE_CODES::contains))
                .isPresent();
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
            return i1 < i2 ? item2 : item1;
        }
    }

    @Override
    public String getApplication(){
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}
