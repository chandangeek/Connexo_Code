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
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreateconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreateconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreateconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreateconfirmation.UUID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreateconfirmation.UtilitiesTimeSeriesERPItemBulkCreateConfirmationCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreateconfirmation.UtilitiesTimeSeriesID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreateconfirmation.UtilsTmeSersERPItmBulkCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreateconfirmation.UtilsTmeSersERPItmCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreateconfirmation.UtilsTmeSersERPItmCrteConfUtilsTmeSers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds.NO_SEVERITY_CODE_AND_ERROR_MESSAGE;
import static com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds.SEVERITY_CODE_AND_ERROR_MESSAGE;

@Component(name = "com.energyict.mdc.sap.soap.webservices.impl.uploadusagedata.UtilitiesTimeSeriesBulkCreateConfirmationReceiver",
        service = {InboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + UtilitiesTimeSeriesBulkCreateConfirmationReceiver.NAME})
public class UtilitiesTimeSeriesBulkCreateConfirmationReceiver extends AbstractInboundEndPoint implements InboundSoapEndPointProvider, UtilitiesTimeSeriesERPItemBulkCreateConfirmationCIn, ApplicationSpecific {
    static final String NAME = "SAP TimeSeriesBulkCreateConfirmation";

    private volatile DataExportServiceCallType dataExportServiceCallType;
    private volatile Thesaurus thesaurus;
    private volatile WebServiceActivator webServiceActivator;

    public UtilitiesTimeSeriesBulkCreateConfirmationReceiver() {
        // for OSGi purposes
    }

    @Inject
    public UtilitiesTimeSeriesBulkCreateConfirmationReceiver(DataExportService dataExportService, Thesaurus thesaurus,
                                                             WebServiceActivator webServiceActivator) {
        setDataExportService(dataExportService);
        setWebServiceActivator(webServiceActivator);
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
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        this.webServiceActivator = webServiceActivator;
    }

    @Override
    public UtilitiesTimeSeriesBulkCreateConfirmationReceiver get() {
        return this;
    }

    @Override
    public void utilitiesTimeSeriesERPItemBulkCreateConfirmationCIn(UtilsTmeSersERPItmBulkCrteConfMsg confirmation) {
        runInTransactionWithOccurrence(() -> {
            SetMultimap<String, String> values = HashMultimap.create();
            getCreateConfirmationMessages(confirmation).stream()
                    .map(UtilitiesTimeSeriesBulkCreateConfirmationReceiver::getProfileId)
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
                case FAILED:// since some error codes should be processed as successful, use tryPartiallyPassingServiceCall for failed case
                    List<String> successfulProfileIds = getSuccessfulProfileIds(confirmation);
                    List<ServiceCall> successfulChildren = new ArrayList<>();
                    serviceCall.findChildren().stream().forEach(child -> {
                        List<String> extensionProfileIds = Arrays.asList(dataExportServiceCallType.getCustomInfoFromChildServiceCall(child).split(","));
                        if (successfulProfileIds.containsAll(extensionProfileIds)) {
                            successfulChildren.add(child);
                        }
                        successfulProfileIds.removeAll(extensionProfileIds);
                    });
                    dataExportServiceCallType.tryPartiallyPassingServiceCall(serviceCall, successfulChildren, getErrorMessage(confirmation).orElse(null));
                    break;
            }
            return null;
        });
    }

    private List<String> getSuccessfulProfileIds(UtilsTmeSersERPItmBulkCrteConfMsg confirmation) {
        return Optional.ofNullable(confirmation)
                .map(UtilsTmeSersERPItmBulkCrteConfMsg::getUtilitiesTimeSeriesERPItemCreateConfirmationMessage)
                .map(List::stream)
                .orElseGet(Stream::empty)
                .filter(this::isChildConfirmed)
                .map(UtilsTmeSersERPItmCrteConfMsg::getUtilitiesTimeSeries)
                .map(UtilsTmeSersERPItmCrteConfUtilsTmeSers::getID)
                .map(UtilitiesTimeSeriesID::getValue)
                .collect(Collectors.toList());
    }

    private boolean isChildConfirmed(UtilsTmeSersERPItmCrteConfMsg item) {
        boolean isSuccessful = false;
        Optional<Log> log = Optional.ofNullable(item)
                .map(UtilsTmeSersERPItmCrteConfMsg::getLog);
        if (log.isPresent()) {
            isSuccessful = log
                    .map(Log::getBusinessDocumentProcessingResultCode)
                    .filter(code -> code.equals(ProcessingResultCode.SUCCESSFUL.getCode()))
                    .isPresent();

            if (!isSuccessful) {
                List<LogItem> logItems = log.get().getItem();
                if (!logItems.isEmpty()) {
                    isSuccessful = logItems
                            .stream()
                            .map(LogItem::getTypeID)
                            .allMatch(typeId -> webServiceActivator.getUudSuccessfulErrorCodes().stream().anyMatch(typeId::startsWith));
                }
            }
        }

        return isSuccessful;
    }

    private static List<UtilsTmeSersERPItmCrteConfMsg> getCreateConfirmationMessages(UtilsTmeSersERPItmBulkCrteConfMsg confirmation) {
        return Optional.ofNullable(confirmation)
                .map(UtilsTmeSersERPItmBulkCrteConfMsg::getUtilitiesTimeSeriesERPItemCreateConfirmationMessage)
                .orElse(Collections.emptyList());
    }

    private static Optional<String> getProfileId(UtilsTmeSersERPItmCrteConfMsg message) {
        return Optional.ofNullable(message)
                .map(UtilsTmeSersERPItmCrteConfMsg::getUtilitiesTimeSeries)
                .map(UtilsTmeSersERPItmCrteConfUtilsTmeSers::getID)
                .map(UtilitiesTimeSeriesID::getValue);
    }

    private static Optional<String> findReferenceUuid(UtilsTmeSersERPItmBulkCrteConfMsg confirmation) {
        return Optional.ofNullable(confirmation)
                .map(UtilsTmeSersERPItmBulkCrteConfMsg::getMessageHeader)
                .flatMap(UtilitiesTimeSeriesBulkCreateConfirmationReceiver::findReferenceUuid);
    }

    private static Optional<String> findReferenceUuid(BusinessDocumentMessageHeader header) {
        return Optional.ofNullable(header.getReferenceUUID()).map(UUID::getValue);
    }

    private static ProcessingResultCode getResultCode(UtilsTmeSersERPItmBulkCrteConfMsg confirmation) {
        return Optional.ofNullable(confirmation)
                .map(UtilsTmeSersERPItmBulkCrteConfMsg::getLog)
                .map(Log::getBusinessDocumentProcessingResultCode)
                .flatMap(ProcessingResultCode::fromCode)
                .orElse(ProcessingResultCode.FAILED);
    }

    private Optional<String> getErrorMessage(UtilsTmeSersERPItmBulkCrteConfMsg confirmation) {
        List<LogItem> list = Optional.ofNullable(confirmation)
                .map(UtilsTmeSersERPItmBulkCrteConfMsg::getLog)
                .map(Log::getItem)
                .map(List::stream)
                .orElseGet(Stream::empty)
                .filter(log -> log.getNote() != null && !Checks.is(log.getNote()).emptyOrOnlyWhiteSpace())
                .collect(Collectors.toList());

        if (list.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(list.stream()
                    .map(log -> {
                        if (log.getSeverityCode() != null && !Checks.is(log.getSeverityCode()).emptyOrOnlyWhiteSpace()) {
                            return thesaurus.getSimpleFormat(SEVERITY_CODE_AND_ERROR_MESSAGE).format(log.getSeverityCode(), log.getNote());
                        } else {
                            return thesaurus.getSimpleFormat(NO_SEVERITY_CODE_AND_ERROR_MESSAGE).format(log.getNote());
                        }
                    })
                    .collect(Collectors.joining(" ")));
        }
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}
