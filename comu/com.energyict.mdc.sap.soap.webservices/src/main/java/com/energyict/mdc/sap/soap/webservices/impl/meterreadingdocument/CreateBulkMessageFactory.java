/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;

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

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.PROCESSING_ERROR_CATEGORY_CODE;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID;

public class CreateBulkMessageFactory {

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    public CreateBulkMessageFactory() {
    }

    public SmrtMtrMtrRdngDocERPBulkCrteConfMsg createMessage(MeterReadingDocumentCreateRequestMessage requestMessage, Instant now) {
        SmrtMtrMtrRdngDocERPBulkCrteConfMsg bulkConfirmationMessage = OBJECT_FACTORY.createSmrtMtrMtrRdngDocERPBulkCrteConfMsg();
        bulkConfirmationMessage.setMessageHeader(createHeader(requestMessage, now));
        bulkConfirmationMessage.setLog(OBJECT_FACTORY.createLog());
        createAndValidateBody(bulkConfirmationMessage, requestMessage.getMeterReadingDocumentCreateMessages(), now);
        return bulkConfirmationMessage;
    }

    public SmrtMtrMtrRdngDocERPBulkCrteConfMsg createMessage(MeterReadingDocumentCreateRequestMessage requestMessage,
                                                             MessageSeeds messageSeeds, Instant now) {
        SmrtMtrMtrRdngDocERPBulkCrteConfMsg bulkConfirmationMessage = OBJECT_FACTORY.createSmrtMtrMtrRdngDocERPBulkCrteConfMsg();
        bulkConfirmationMessage.setMessageHeader(createHeader(requestMessage, now));
        bulkConfirmationMessage.setLog(OBJECT_FACTORY.createLog());
        bulkConfirmationMessage.getLog().getItem().add(createLogItem(messageSeeds));
        createAndValidateBody(bulkConfirmationMessage, requestMessage.getMeterReadingDocumentCreateMessages(), now);
        return bulkConfirmationMessage;
    }

    private BusinessDocumentMessageHeader createHeader(MeterReadingDocumentCreateRequestMessage requestMessage, Instant now) {

        BusinessDocumentMessageHeader messageHeader = OBJECT_FACTORY.createBusinessDocumentMessageHeader();

        if (!Strings.isNullOrEmpty(requestMessage.getId())){
            BusinessDocumentMessageID id = OBJECT_FACTORY.createBusinessDocumentMessageID();
            id.setValue(requestMessage.getId());
            messageHeader.setReferenceID(id);
        }

        if (!Strings.isNullOrEmpty(requestMessage.getUuid())){
            UUID uuid = OBJECT_FACTORY.createUUID();
            uuid.setValue(requestMessage.getUuid());
            messageHeader.setReferenceUUID(uuid);
        }

        messageHeader.setCreationDateTime(now);

        return messageHeader;
    }

    private SmrtMtrMtrRdngDocERPCrteConfMsg createBody(MeterReadingDocumentCreateMessage message,
                                                       LogItem logItem,
                                                       Instant now) {
        MeterReadingDocumentID meterReadingDocumentID = OBJECT_FACTORY.createMeterReadingDocumentID();
        meterReadingDocumentID.setValue(message.getId());

        BusinessDocumentMessageHeader documentMessageHeader = OBJECT_FACTORY.createBusinessDocumentMessageHeader();

        if (!Strings.isNullOrEmpty(message.getHeaderId())) {
            BusinessDocumentMessageID referenceId = OBJECT_FACTORY.createBusinessDocumentMessageID();
            referenceId.setValue(message.getHeaderId());
            documentMessageHeader.setReferenceID(referenceId);
        }

        if (!Strings.isNullOrEmpty(message.getHeaderUUID())) {
            UUID uuid = OBJECT_FACTORY.createUUID();
            uuid.setValue(message.getHeaderUUID());
            documentMessageHeader.setReferenceUUID(uuid);
        }

        documentMessageHeader.setCreationDateTime(now);

        SmrtMtrMtrRdngDocERPCrteConfMtrRdngDoc document = OBJECT_FACTORY.createSmrtMtrMtrRdngDocERPCrteConfMtrRdngDoc();
        document.setID(meterReadingDocumentID);

        SmrtMtrMtrRdngDocERPCrteConfMsg confirmationMessage = OBJECT_FACTORY.createSmrtMtrMtrRdngDocERPCrteConfMsg();
        confirmationMessage.setMessageHeader(documentMessageHeader);
        confirmationMessage.setMeterReadingDocument(document);

        Log log = OBJECT_FACTORY.createLog();
        log.getItem().add(logItem);

        confirmationMessage.setLog(log);

        return confirmationMessage;
    }

    private void createAndValidateBody(SmrtMtrMtrRdngDocERPBulkCrteConfMsg bulkConfirmationMessage,
                                       List<MeterReadingDocumentCreateMessage> messages,
                                       Instant now) {

        /*SmrtMtrMtrRdngDocERPCrteConfMtrRdngDoc meterReadingDocument =
                OBJECT_FACTORY.createSmrtMtrMtrRdngDocERPCrteConfMtrRdngDoc();*/

        messages.forEach(message -> {
            if (!message.isValid()) {
                System.out.println("MESSAGE IS NOT VALID!!!!!!!!!");
                bulkConfirmationMessage.getSmartMeterMeterReadingDocumentERPCreateConfirmationMessage()
                        .add(createBody(message, createLogItem(MessageSeeds.INVALID_METER_READING_DOCUMENT, message.getId()), now));
                /*bulkConfirmationMessage.getLog()
                        .getItem()
                        .add(createLogItem(MessageSeeds.INVALID_METER_READING_DOCUMENT, message.getId()));*/
            } else if (!message.isBulkSupported()) {
                System.out.println("BULK IS NOT SUPPORTED!!!!!!!!!");
                bulkConfirmationMessage.getSmartMeterMeterReadingDocumentERPCreateConfirmationMessage()
                        .add(createBody(message, createLogItem(MessageSeeds.UNSUPPORTED_REASON_CODE, message.getId()), now));
                /*bulkConfirmationMessage.getLog()
                        .getItem()
                        .add(createLogItem(MessageSeeds.UNSUPPORTED_REASON_CODE, message.getId()));*/
            } else {
                System.out.println("MESSAGE IS OK!!!!!!!!!");
                bulkConfirmationMessage.getSmartMeterMeterReadingDocumentERPCreateConfirmationMessage()
                        .add(createBody(message, null/*OBJECT_FACTORY.createLogItem()*/, now));
            }
        });
        //bulkConfirmationMessage.getLog().getItem();
    }

    private LogItem createLogItem(MessageSeeds messageSeeds, Object... args) {
        LogItemCategoryCode logItemCategoryCode = OBJECT_FACTORY.createLogItemCategoryCode();
        logItemCategoryCode.setValue(PROCESSING_ERROR_CATEGORY_CODE);

        LogItem logItem = OBJECT_FACTORY.createLogItem();
        logItem.setTypeID(UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID);
        logItem.setCategoryCode(logItemCategoryCode);
        logItem.setNote(messageSeeds.getDefaultFormat(args));

        return logItem;
    }
}