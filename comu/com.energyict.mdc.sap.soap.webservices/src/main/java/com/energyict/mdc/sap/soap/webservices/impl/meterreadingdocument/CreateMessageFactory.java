/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.webservices.impl.SeverityCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.LogItemCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.MeterReadingDocumentID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.SmrtMtrMtrRdngDocERPCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.SmrtMtrMtrRdngDocERPCrteConfMtrRdngDoc;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.UUID;

import com.google.common.base.Strings;

import java.time.Instant;
import java.util.OptionalInt;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.PROCESSING_ERROR_CATEGORY_CODE;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.SUCCESSFUL_PROCESSING_TYPE_ID;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID;

public class CreateMessageFactory {

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    public CreateMessageFactory() {
    }

    public SmrtMtrMtrRdngDocERPCrteConfMsg createMessage(MeterReadingDocumentCreateRequestMessage requestMessage, Instant now) {
        SmrtMtrMtrRdngDocERPCrteConfMsg confirmationMessage = OBJECT_FACTORY.createSmrtMtrMtrRdngDocERPCrteConfMsg();
        confirmationMessage.setMessageHeader(createHeader(requestMessage, now));

        requestMessage.getMeterReadingDocumentCreateMessages()
                .forEach(message -> {
                    if (!message.isValid()) {
                        confirmationMessage
                                .setLog(createLog(MessageSeeds.INVALID_METER_READING_DOCUMENT,
                                        PROCESSING_ERROR_CATEGORY_CODE,
                                        UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID,
                                        ProcessingResultCode.FAILED.getCode(),
                                        message.getId()));
                        confirmationMessage.setMeterReadingDocument(createBody(message));
                    } else if (!message.isSingleSupported()) {
                        confirmationMessage
                                .setLog(createLog(MessageSeeds.UNSUPPORTED_REASON_CODE,
                                        PROCESSING_ERROR_CATEGORY_CODE,
                                        UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID,
                                        ProcessingResultCode.FAILED.getCode(),
                                        message.getId()));
                        confirmationMessage.setMeterReadingDocument(createBody(message));
                    } else {
                        confirmationMessage.setMeterReadingDocument(createBody(message));
                        confirmationMessage.setLog(createLog(MessageSeeds.OK_RESULT,
                                null,
                                SUCCESSFUL_PROCESSING_TYPE_ID,
                                ProcessingResultCode.SUCCESSFUL.getCode()));
                    }
                });

        return confirmationMessage;
    }

    public SmrtMtrMtrRdngDocERPCrteConfMsg createMessage(MeterReadingDocumentCreateRequestMessage requestMessage,
                                                         MessageSeeds messageSeeds, Instant now) {
        SmrtMtrMtrRdngDocERPCrteConfMsg confirmationMessage = OBJECT_FACTORY.createSmrtMtrMtrRdngDocERPCrteConfMsg();

        confirmationMessage.setMessageHeader(createHeader(requestMessage, now));
        confirmationMessage.setLog(createLog(messageSeeds, PROCESSING_ERROR_CATEGORY_CODE, UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID, ProcessingResultCode.FAILED.getCode()));
        requestMessage.getMeterReadingDocumentCreateMessages()
                .forEach(message -> {
                    confirmationMessage.setMeterReadingDocument(createBody(message));
                });
        return confirmationMessage;
    }

    private BusinessDocumentMessageHeader createHeader(MeterReadingDocumentCreateRequestMessage requestMessage, Instant now) {

        BusinessDocumentMessageHeader messageHeader = OBJECT_FACTORY.createBusinessDocumentMessageHeader();
        String uuid = java.util.UUID.randomUUID().toString();

        if (!Strings.isNullOrEmpty(requestMessage.getId())) {
            BusinessDocumentMessageID messageID = OBJECT_FACTORY.createBusinessDocumentMessageID();
            messageID.setValue(requestMessage.getId());
            messageHeader.setReferenceID(messageID);
        }

        messageHeader.setUUID(createUUID(uuid));
        if (!Strings.isNullOrEmpty(requestMessage.getUuid())) {
            messageHeader.setReferenceUUID(createUUID(requestMessage.getUuid()));
        }

        messageHeader.setCreationDateTime(now);

        return messageHeader;
    }

    private UUID createUUID(String uuid) {
        UUID messageUUID = OBJECT_FACTORY.createUUID();
        messageUUID.setValue(uuid);
        return messageUUID;
    }

    private SmrtMtrMtrRdngDocERPCrteConfMtrRdngDoc createBody(MeterReadingDocumentCreateMessage message) {
        MeterReadingDocumentID meterReadingDocumentID = OBJECT_FACTORY.createMeterReadingDocumentID();
        meterReadingDocumentID.setValue(message.getId());

        SmrtMtrMtrRdngDocERPCrteConfMtrRdngDoc meterReadingDocument = OBJECT_FACTORY.createSmrtMtrMtrRdngDocERPCrteConfMtrRdngDoc();
        meterReadingDocument.setID(meterReadingDocumentID);

        return meterReadingDocument;
    }

    private Log createLog(MessageSeeds messageSeeds, String categoryCode, String typeId, String docProcResultCode, Object... args) {

        LogItem logItem = OBJECT_FACTORY.createLogItem();

        if (!Strings.isNullOrEmpty(categoryCode)) {
            LogItemCategoryCode logItemCategoryCode = OBJECT_FACTORY.createLogItemCategoryCode();
            logItemCategoryCode.setValue(categoryCode);
            logItem.setCategoryCode(logItemCategoryCode);
        }

        logItem.setTypeID(typeId);
        logItem.setSeverityCode(SeverityCode.getSeverityCode(messageSeeds.getLevel()));

        logItem.setNote(messageSeeds.getDefaultFormat(args));

        Log log = OBJECT_FACTORY.createLog();
        log.setBusinessDocumentProcessingResultCode(docProcResultCode);
        log.getItem().add(logItem);
        setMaximumLogItemSeverityCode(log);

        return log;
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