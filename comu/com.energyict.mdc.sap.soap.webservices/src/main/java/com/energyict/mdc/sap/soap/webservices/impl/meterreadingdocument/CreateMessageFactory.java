/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
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

import java.time.Instant;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.PROCESSING_ERROR_CATEGORY_CODE;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.UNSUCCESSFUL_PROCESSING_CODE;

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
                                .setLog(createLog(MessageSeeds.INVALID_METER_READING_DOCUMENT, message.getId()));
                    } else if (!message.isSingleSupported()) {
                        confirmationMessage
                                .setLog(createLog(MessageSeeds.UNSUPPORTED_REASON_CODE, message.getId()));
                    } else {
                        confirmationMessage.setMeterReadingDocument(createBody(message));
                    }
                });

        return confirmationMessage;
    }

    public SmrtMtrMtrRdngDocERPCrteConfMsg createMessage(MeterReadingDocumentCreateRequestMessage requestMessage,
                                                         MessageSeeds messageSeeds, Instant now) {
        SmrtMtrMtrRdngDocERPCrteConfMsg confirmationMessage = OBJECT_FACTORY.createSmrtMtrMtrRdngDocERPCrteConfMsg();

        confirmationMessage.setMessageHeader(createHeader(requestMessage, now));
        confirmationMessage.setLog(createLog(messageSeeds));

        return confirmationMessage;
    }

    private BusinessDocumentMessageHeader createHeader(MeterReadingDocumentCreateRequestMessage requestMessage, Instant now) {
        BusinessDocumentMessageID messageID = OBJECT_FACTORY.createBusinessDocumentMessageID();
        messageID.setValue(requestMessage.getId());

        BusinessDocumentMessageHeader messageHeader = OBJECT_FACTORY.createBusinessDocumentMessageHeader();
        messageHeader.setReferenceID(messageID);
        UUID uuid = OBJECT_FACTORY.createUUID();
        uuid.setValue(requestMessage.getUuid());
        messageHeader.setReferenceUUID(uuid);
        messageHeader.setCreationDateTime(now);

        return messageHeader;
    }


    private SmrtMtrMtrRdngDocERPCrteConfMtrRdngDoc createBody(MeterReadingDocumentCreateMessage message) {
        MeterReadingDocumentID meterReadingDocumentID = OBJECT_FACTORY.createMeterReadingDocumentID();
        meterReadingDocumentID.setValue(message.getId());

        SmrtMtrMtrRdngDocERPCrteConfMtrRdngDoc meterReadingDocument = OBJECT_FACTORY.createSmrtMtrMtrRdngDocERPCrteConfMtrRdngDoc();
        meterReadingDocument.setID(meterReadingDocumentID);

        return meterReadingDocument;
    }

    private Log createLog(MessageSeeds messageSeeds, Object... args) {
        LogItemCategoryCode logItemCategoryCode = OBJECT_FACTORY.createLogItemCategoryCode();
        logItemCategoryCode.setValue(PROCESSING_ERROR_CATEGORY_CODE);

        LogItem logItem = OBJECT_FACTORY.createLogItem();
        logItem.setTypeID(UNSUCCESSFUL_PROCESSING_CODE);
        logItem.setCategoryCode(logItemCategoryCode);
        logItem.setNote(messageSeeds.getDefaultFormat(args));

        Log log = OBJECT_FACTORY.createLog();
        log.getItem().add(logItem);
        return log;
    }
}