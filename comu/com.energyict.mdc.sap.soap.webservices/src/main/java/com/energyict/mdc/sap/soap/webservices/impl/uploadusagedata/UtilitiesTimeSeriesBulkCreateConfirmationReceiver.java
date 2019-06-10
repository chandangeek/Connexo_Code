/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.uploadusagedata;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.webservicecall.DataExportServiceCallType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.InboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreateconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreateconfirmation.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreateconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreateconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreateconfirmation.UUID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreateconfirmation.UtilitiesTimeSeriesERPItemBulkCreateConfirmationEIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreateconfirmation.UtilsTmeSersERPItmBulkCrteConfMsg;

import com.google.common.collect.ImmutableSet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Component(name = UtilitiesTimeSeriesBulkCreateConfirmationReceiver.NAME,
        service = {InboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + UtilitiesTimeSeriesBulkCreateConfirmationReceiver.NAME})
public class UtilitiesTimeSeriesBulkCreateConfirmationReceiver extends AbstractInboundEndPoint implements InboundSoapEndPointProvider, UtilitiesTimeSeriesERPItemBulkCreateConfirmationEIn, ApplicationSpecific {
    static final String NAME = "SAP UtilitiesTimeSeriesERPItemBulkCreateConfirmation_C_In";
    private static final Set<String> FAILURE_CODES = ImmutableSet.of("5");

    private volatile DataExportServiceCallType dataExportServiceCallType;
    private volatile Thesaurus thesaurus;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile UserService userService;

    public UtilitiesTimeSeriesBulkCreateConfirmationReceiver() {
        // for OSGi purposes
    }

    @Inject
    public UtilitiesTimeSeriesBulkCreateConfirmationReceiver(DataExportService dataExportService, Thesaurus thesaurus) {
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

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UtilitiesTimeSeriesBulkCreateConfirmationReceiver get() {
        return this;
    }

    @Override
    public void utilitiesTimeSeriesERPItemBulkCreateConfirmationEIn(UtilsTmeSersERPItmBulkCrteConfMsg confirmation) {
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

    private static Optional<String> findReferenceUuid(UtilsTmeSersERPItmBulkCrteConfMsg confirmation) {
        return Optional.ofNullable(confirmation)
                .map(UtilsTmeSersERPItmBulkCrteConfMsg::getMessageHeader)
                .flatMap(UtilitiesTimeSeriesBulkCreateConfirmationReceiver::findReferenceUuid);
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

    private static boolean isConfirmed(UtilsTmeSersERPItmBulkCrteConfMsg confirmation) {
        return Optional.ofNullable(confirmation)
                .map(UtilsTmeSersERPItmBulkCrteConfMsg::getLog)
                .map(Log::getBusinessDocumentProcessingResultCode)
                .filter(Predicates.not(FAILURE_CODES::contains))
                .isPresent();
    }

    private static Optional<String> getSeverestError(UtilsTmeSersERPItmBulkCrteConfMsg confirmation) {
        return Optional.ofNullable(confirmation)
                .map(UtilsTmeSersERPItmBulkCrteConfMsg::getLog)
                .map(Log::getItem)
                .map(List::stream)
                .orElseGet(Stream::empty)
                .reduce(UtilitiesTimeSeriesBulkCreateConfirmationReceiver::findMaximumSeverityOrNotNullOrWhatever)
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
