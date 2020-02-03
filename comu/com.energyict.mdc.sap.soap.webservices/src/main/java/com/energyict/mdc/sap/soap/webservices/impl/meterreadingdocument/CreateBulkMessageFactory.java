/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;

import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.webservices.impl.SeverityCode;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallHelper;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MasterMeterReadingDocumentCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.LogItemCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.MeterReadingDocumentID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.SmrtMtrMtrRdngDocERPBulkCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.SmrtMtrMtrRdngDocERPCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.SmrtMtrMtrRdngDocERPCrteConfMtrRdngDoc;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.UUID;

import com.google.common.base.Strings;

import java.time.Instant;
import java.util.List;
import java.util.OptionalInt;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.PROCESSING_ERROR_CATEGORY_CODE;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.SUCCESSFUL_PROCESSING_TYPE_ID;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID;

public class CreateBulkMessageFactory {

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    CreateBulkMessageFactory() {
    }

    public SmrtMtrMtrRdngDocERPBulkCrteConfMsg createMessage(MasterMeterReadingDocumentCreateRequestDomainExtension extension,
                                                             List<ServiceCall> children,
                                                             Instant now,
                                                             String senderBusinessSystemId) {
        SmrtMtrMtrRdngDocERPBulkCrteConfMsg bulkConfirmationMessage = OBJECT_FACTORY.createSmrtMtrMtrRdngDocERPBulkCrteConfMsg();
        bulkConfirmationMessage.setMessageHeader(createHeader(extension.getRequestID(), extension.getUuid(), now, senderBusinessSystemId));

        ServiceCall parent = extension.getServiceCall();
        switch (parent.getState()) {
            case CANCELLED:
                if (ServiceCallHelper.hasAllChildrenInState(children, DefaultState.CANCELLED)) {
                    bulkConfirmationMessage.setLog(createFailedLog(MessageSeeds.REQUEST_CANCELLED.getDefaultFormat(new Object[0])));
                } else if (ServiceCallHelper.hasAnyChildState(children, DefaultState.SUCCESSFUL)) {
                    bulkConfirmationMessage.setLog(createPartiallySuccessfulLog());
                } else {
                    bulkConfirmationMessage.setLog(createFailedLog(MessageSeeds.BULK_REQUEST_WAS_FAILED.getDefaultFormat(new Object[0])));
                }
                break;
            case SUCCESSFUL:
                bulkConfirmationMessage.setLog(createSuccessfulLog());
                break;
            case FAILED:
                bulkConfirmationMessage.setLog(createFailedLog(MessageSeeds.BULK_REQUEST_WAS_FAILED.getDefaultFormat(new Object[0])));
                break;
            case PARTIAL_SUCCESS:
                bulkConfirmationMessage.setLog(createPartiallySuccessfulLog());
                break;
            default:
                // No specific action required for these states
                break;
        }

        children.forEach(child -> {
            MeterReadingDocumentCreateRequestDomainExtension extensionChild = child.getExtensionFor(new MeterReadingDocumentCreateRequestCustomPropertySet()).get();
            if (child.getState() == DefaultState.SUCCESSFUL) {
                bulkConfirmationMessage.getSmartMeterMeterReadingDocumentERPCreateConfirmationMessage()
                        .add(createChildMessage(extensionChild.getMeterReadingDocumentId(), extensionChild.getReferenceID(), extensionChild.getReferenceUuid(),
                                createSuccessfulLog(), senderBusinessSystemId, now));
            } else if (child.getState() == DefaultState.FAILED || child.getState() == DefaultState.CANCELLED) {
                bulkConfirmationMessage.getSmartMeterMeterReadingDocumentERPCreateConfirmationMessage()
                        .add(createChildMessage(extensionChild.getMeterReadingDocumentId(), extensionChild.getReferenceID(), extensionChild.getReferenceUuid(),
                                createFailedLog(extensionChild.getErrorMessage()), senderBusinessSystemId, now));
            }

        });
        return bulkConfirmationMessage;
    }

    public SmrtMtrMtrRdngDocERPBulkCrteConfMsg createMessage(MeterReadingDocumentCreateRequestMessage requestMessage,
                                                             MessageSeeds messageSeeds,
                                                             Instant now,
                                                             String senderBusinessSystemId) {
        SmrtMtrMtrRdngDocERPBulkCrteConfMsg bulkConfirmationMessage = OBJECT_FACTORY.createSmrtMtrMtrRdngDocERPBulkCrteConfMsg();
        bulkConfirmationMessage.setMessageHeader(createHeader(requestMessage.getId(), requestMessage.getUuid(), now, senderBusinessSystemId));
        bulkConfirmationMessage.setLog(createFailedLog(messageSeeds.getDefaultFormat()));
        bulkConfirmationMessage.getLog().setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
        setMaximumLogItemSeverityCode(bulkConfirmationMessage.getLog());

        requestMessage.getMeterReadingDocumentCreateMessages().forEach(message -> bulkConfirmationMessage.getSmartMeterMeterReadingDocumentERPCreateConfirmationMessage()
                .add(createChildMessage(message.getId(), message.getHeaderId(), message.getHeaderUUID(),
                        createFailedLog(MessageSeeds.BULK_ITEM_PROCESSING_WAS_NOT_STARTED.getDefaultFormat(new Object[0])), senderBusinessSystemId, now)));

        return bulkConfirmationMessage;
    }

    private BusinessDocumentMessageHeader createHeader(String requestId, String requestUuid,
                                                       Instant now,
                                                       String senderBusinessSystemId) {
        BusinessDocumentMessageHeader messageHeader = OBJECT_FACTORY.createBusinessDocumentMessageHeader();
        String uuid = java.util.UUID.randomUUID().toString();

        if (!Strings.isNullOrEmpty(requestId)) {
            BusinessDocumentMessageID id = OBJECT_FACTORY.createBusinessDocumentMessageID();
            id.setValue(requestId);
            messageHeader.setReferenceID(id);
        }

        messageHeader.setUUID(createUUID(uuid));
        if (!Strings.isNullOrEmpty(requestUuid)) {
            messageHeader.setReferenceUUID(createUUID(requestUuid));
        }

        messageHeader.setSenderBusinessSystemID(senderBusinessSystemId);
        messageHeader.setReconciliationIndicator(true);
        messageHeader.setCreationDateTime(now);

        return messageHeader;
    }

    private UUID createUUID(String uuid) {
        UUID messageUUID = OBJECT_FACTORY.createUUID();
        messageUUID.setValue(uuid);
        return messageUUID;
    }

    private SmrtMtrMtrRdngDocERPCrteConfMsg createChildMessage(String meterReadingDocument,
                                                               String headerId,
                                                               String headerUuid,
                                                               Log log,
                                                               String senderBusinessSystemId,
                                                               Instant now) {
        MeterReadingDocumentID meterReadingDocumentID = OBJECT_FACTORY.createMeterReadingDocumentID();
        meterReadingDocumentID.setValue(meterReadingDocument);

        BusinessDocumentMessageHeader documentMessageHeader = OBJECT_FACTORY.createBusinessDocumentMessageHeader();
        String uuid = java.util.UUID.randomUUID().toString();

        if (!Strings.isNullOrEmpty(headerId)) {
            BusinessDocumentMessageID referenceId = OBJECT_FACTORY.createBusinessDocumentMessageID();
            referenceId.setValue(headerId);
            documentMessageHeader.setReferenceID(referenceId);
        }

        documentMessageHeader.setUUID(createUUID(uuid));
        if (!Strings.isNullOrEmpty(headerUuid)) {
            documentMessageHeader.setReferenceUUID(createUUID(headerUuid));
        }

        documentMessageHeader.setSenderBusinessSystemID(senderBusinessSystemId);
        documentMessageHeader.setReconciliationIndicator(true);
        documentMessageHeader.setCreationDateTime(now);

        SmrtMtrMtrRdngDocERPCrteConfMtrRdngDoc document = OBJECT_FACTORY.createSmrtMtrMtrRdngDocERPCrteConfMtrRdngDoc();
        document.setID(meterReadingDocumentID);

        SmrtMtrMtrRdngDocERPCrteConfMsg confirmationMessage = OBJECT_FACTORY.createSmrtMtrMtrRdngDocERPCrteConfMsg();
        confirmationMessage.setMessageHeader(documentMessageHeader);
        confirmationMessage.setMeterReadingDocument(document);

        confirmationMessage.setLog(log);

        return confirmationMessage;
    }

    private Log createSuccessfulLog() {
        Log log = OBJECT_FACTORY.createLog();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.SUCCESSFUL.getCode());
        log.getItem().add(createLogItem(MessageSeeds.OK_RESULT.getDefaultFormat(new Object[0]),
                SUCCESSFUL_PROCESSING_TYPE_ID, SeverityCode.INFORMATION.getCode(),
                null));
        setMaximumLogItemSeverityCode(log);

        return log;
    }

    private Log createPartiallySuccessfulLog() {
        Log log = OBJECT_FACTORY.createLog();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.PARTIALLY_SUCCESSFUL.getCode());
        log.getItem().add(createLogItem(MessageSeeds.PARTIALLY_SUCCESSFUL.getDefaultFormat(new Object[0]),
                UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID, SeverityCode.ERROR.getCode(),
                null));
        setMaximumLogItemSeverityCode(log);

        return log;
    }

    private Log createFailedLog(String message) {
        Log log = OBJECT_FACTORY.createLog();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
        log.getItem().add(createLogItem(message, UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID,
                SeverityCode.ERROR.getCode(), PROCESSING_ERROR_CATEGORY_CODE));
        setMaximumLogItemSeverityCode(log);

        return log;
    }

    private LogItem createLogItem(String message, String typeId, String severityCode, String categoryCode) {
        LogItem logItem = OBJECT_FACTORY.createLogItem();
        if (!Strings.isNullOrEmpty(categoryCode)) {
            LogItemCategoryCode logItemCategoryCode = OBJECT_FACTORY.createLogItemCategoryCode();
            logItemCategoryCode.setValue(categoryCode);
            logItem.setCategoryCode(logItemCategoryCode);
        }
        logItem.setSeverityCode(severityCode);
        logItem.setTypeID(typeId);
        logItem.setNote(message);

        return logItem;
    }

    private void setMaximumLogItemSeverityCode(Log log) {
        OptionalInt maxInt = log.getItem().stream().map(LogItem::getSeverityCode)
                .filter(Predicates.not(Strings::isNullOrEmpty))
                .mapToInt(Integer::parseInt)
                .max();
        if (maxInt.isPresent()) {
            int value = maxInt.getAsInt();
            log.setMaximumLogItemSeverityCode(Integer.toString(value));
        }
    }
}