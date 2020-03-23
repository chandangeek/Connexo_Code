/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;

import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.webservices.impl.SeverityCode;
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

    public CreateBulkMessageFactory() {
    }

    public SmrtMtrMtrRdngDocERPBulkCrteConfMsg createMessage(MeterReadingDocumentCreateRequestMessage requestMessage,
                                                             Instant now,
                                                             String senderBusinessSystemId) {
        SmrtMtrMtrRdngDocERPBulkCrteConfMsg bulkConfirmationMessage = OBJECT_FACTORY.createSmrtMtrMtrRdngDocERPBulkCrteConfMsg();
        bulkConfirmationMessage.setMessageHeader(createHeader(requestMessage, now, senderBusinessSystemId));
        bulkConfirmationMessage.setLog(OBJECT_FACTORY.createLog());
        bulkConfirmationMessage.getLog().getItem().add(createLogItem(MessageSeeds.OK_RESULT,
                null,
                SUCCESSFUL_PROCESSING_TYPE_ID));
        bulkConfirmationMessage.getLog().setBusinessDocumentProcessingResultCode(ProcessingResultCode.SUCCESSFUL.getCode());
        setMaximumLogItemSeverityCode(bulkConfirmationMessage.getLog());

        createAndValidateBody(bulkConfirmationMessage, requestMessage.getMeterReadingDocumentCreateMessages(), now, senderBusinessSystemId);
        return bulkConfirmationMessage;
    }

    public SmrtMtrMtrRdngDocERPBulkCrteConfMsg createMessage(MeterReadingDocumentCreateRequestMessage requestMessage,
                                                             MessageSeeds messageSeeds,
                                                             Instant now,
                                                             String senderBusinessSystemId) {
        SmrtMtrMtrRdngDocERPBulkCrteConfMsg bulkConfirmationMessage = OBJECT_FACTORY.createSmrtMtrMtrRdngDocERPBulkCrteConfMsg();
        bulkConfirmationMessage.setMessageHeader(createHeader(requestMessage, now, senderBusinessSystemId));
        bulkConfirmationMessage.setLog(OBJECT_FACTORY.createLog());
        bulkConfirmationMessage.getLog().getItem().add(createLogItem(messageSeeds,
                PROCESSING_ERROR_CATEGORY_CODE,
                UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID));
        bulkConfirmationMessage.getLog().setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
        setMaximumLogItemSeverityCode(bulkConfirmationMessage.getLog());

        createAndValidateBody(bulkConfirmationMessage, requestMessage.getMeterReadingDocumentCreateMessages(), now, senderBusinessSystemId);

        return bulkConfirmationMessage;
    }

    private BusinessDocumentMessageHeader createHeader(MeterReadingDocumentCreateRequestMessage requestMessage,
                                                       Instant now,
                                                       String senderBusinessSystemId) {
        BusinessDocumentMessageHeader messageHeader = OBJECT_FACTORY.createBusinessDocumentMessageHeader();
        String uuid = java.util.UUID.randomUUID().toString();

        if (!Strings.isNullOrEmpty(requestMessage.getId())) {
            BusinessDocumentMessageID id = OBJECT_FACTORY.createBusinessDocumentMessageID();
            id.setValue(requestMessage.getId());
            messageHeader.setReferenceID(id);
        }

        messageHeader.setUUID(createUUID(uuid));
        if (!Strings.isNullOrEmpty(requestMessage.getUuid())) {
            messageHeader.setReferenceUUID(createUUID(requestMessage.getUuid()));
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

    private SmrtMtrMtrRdngDocERPCrteConfMsg createBody(MeterReadingDocumentCreateMessage message,
                                                       String docProcResultCode,
                                                       LogItem logItem,
                                                       Instant now,
                                                       String senderBusinessSystemId) {
        MeterReadingDocumentID meterReadingDocumentID = OBJECT_FACTORY.createMeterReadingDocumentID();
        meterReadingDocumentID.setValue(message.getId());

        BusinessDocumentMessageHeader documentMessageHeader = OBJECT_FACTORY.createBusinessDocumentMessageHeader();
        String uuid = java.util.UUID.randomUUID().toString();

        if (!Strings.isNullOrEmpty(message.getHeaderId())) {
            BusinessDocumentMessageID referenceId = OBJECT_FACTORY.createBusinessDocumentMessageID();
            referenceId.setValue(message.getHeaderId());
            documentMessageHeader.setReferenceID(referenceId);
        }

        documentMessageHeader.setUUID(createUUID(uuid));
        if (!Strings.isNullOrEmpty(message.getHeaderUUID())) {
            documentMessageHeader.setReferenceUUID(createUUID(message.getHeaderUUID()));
        }

        documentMessageHeader.setSenderBusinessSystemID(senderBusinessSystemId);
        documentMessageHeader.setReconciliationIndicator(true);
        documentMessageHeader.setCreationDateTime(now);

        SmrtMtrMtrRdngDocERPCrteConfMtrRdngDoc document = OBJECT_FACTORY.createSmrtMtrMtrRdngDocERPCrteConfMtrRdngDoc();
        document.setID(meterReadingDocumentID);

        SmrtMtrMtrRdngDocERPCrteConfMsg confirmationMessage = OBJECT_FACTORY.createSmrtMtrMtrRdngDocERPCrteConfMsg();
        confirmationMessage.setMessageHeader(documentMessageHeader);
        confirmationMessage.setMeterReadingDocument(document);

        Log log = OBJECT_FACTORY.createLog();
        log.getItem().add(logItem);
        log.setBusinessDocumentProcessingResultCode(docProcResultCode);
        setMaximumLogItemSeverityCode(log);

        confirmationMessage.setLog(log);

        return confirmationMessage;
    }

    private void createAndValidateBody(SmrtMtrMtrRdngDocERPBulkCrteConfMsg bulkConfirmationMessage,
                                       List<MeterReadingDocumentCreateMessage> messages,
                                       Instant now,
                                       String senderBusinessSystemId) {
        messages.forEach(message -> {
            if (!message.isValid()) {
                bulkConfirmationMessage.getSmartMeterMeterReadingDocumentERPCreateConfirmationMessage()
                        .add(createBody(message, ProcessingResultCode.FAILED.getCode(), createLogItem(MessageSeeds.INVALID_METER_READING_DOCUMENT,
                                PROCESSING_ERROR_CATEGORY_CODE,
                                UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID,
                                message.getId()), now, senderBusinessSystemId));
            } else if (!message.isBulkSupported()) {
                bulkConfirmationMessage.getSmartMeterMeterReadingDocumentERPCreateConfirmationMessage()
                        .add(createBody(message, ProcessingResultCode.FAILED.getCode(), createLogItem(MessageSeeds.UNSUPPORTED_REASON_CODE,
                                PROCESSING_ERROR_CATEGORY_CODE,
                                UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID,
                                message.getId()), now, senderBusinessSystemId));
            } else {
                bulkConfirmationMessage.getSmartMeterMeterReadingDocumentERPCreateConfirmationMessage()
                        .add(createBody(message, ProcessingResultCode.SUCCESSFUL.getCode(), createLogItem(MessageSeeds.OK_RESULT,
                                null,
                                SUCCESSFUL_PROCESSING_TYPE_ID), now, senderBusinessSystemId));
            }
        });
    }

    private LogItem createLogItem(MessageSeeds messageSeeds, String categoryCode, String typeId, Object... args) {

        LogItem logItem = OBJECT_FACTORY.createLogItem();

        if (!Strings.isNullOrEmpty(categoryCode)) {
            LogItemCategoryCode logItemCategoryCode = OBJECT_FACTORY.createLogItemCategoryCode();
            logItemCategoryCode.setValue(categoryCode);
            logItem.setCategoryCode(logItemCategoryCode);
        }

        logItem.setTypeID(typeId);
        logItem.setSeverityCode(SeverityCode.getSeverityCode(messageSeeds.getLevel()));
        logItem.setNote(messageSeeds.getDefaultFormat(args));

        return logItem;
    }

    private void setMaximumLogItemSeverityCode(Log log) {
        OptionalInt maxInt = log.getItem().stream().map(LogItem::getSeverityCode)
                .filter(Predicates.not(Strings::isNullOrEmpty))
                .mapToInt(Integer::parseInt)
                .max();
        if (maxInt.isPresent()) {
            Integer value = maxInt.getAsInt();
            log.setMaximumLogItemSeverityCode(value.toString());
        }
    }
}