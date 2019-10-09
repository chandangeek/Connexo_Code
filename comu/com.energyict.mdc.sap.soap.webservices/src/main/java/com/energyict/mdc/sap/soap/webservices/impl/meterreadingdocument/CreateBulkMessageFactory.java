/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;

import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.LogItemCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.MeterReadingDocumentID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.SmrtMtrMtrRdngDocERPBulkCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.SmrtMtrMtrRdngDocERPCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.SmrtMtrMtrRdngDocERPCrteConfMtrRdngDoc;

import java.time.Instant;
import java.util.List;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.PROCESSING_ERROR_CATEGORY_CODE;

public class CreateBulkMessageFactory {

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    public CreateBulkMessageFactory() {
    }

    public SmrtMtrMtrRdngDocERPBulkCrteConfMsg createMessage(MeterReadingDocumentCreateRequestMessage requestMessage, Instant now) {
        SmrtMtrMtrRdngDocERPBulkCrteConfMsg bulkConfirmationMessage = OBJECT_FACTORY.createSmrtMtrMtrRdngDocERPBulkCrteConfMsg();
        bulkConfirmationMessage.setMessageHeader(createHeader(requestMessage, now));
        bulkConfirmationMessage.setLog(OBJECT_FACTORY.createLog());
        createAndValidateBody(bulkConfirmationMessage, requestMessage.getMeterReadingDocumentCreateMessages());
        return bulkConfirmationMessage;
    }

    public SmrtMtrMtrRdngDocERPBulkCrteConfMsg createMessage(MeterReadingDocumentCreateRequestMessage requestMessage,
                                                             MessageSeeds messageSeeds, Instant now) {
        SmrtMtrMtrRdngDocERPBulkCrteConfMsg bulkConfirmationMessage = OBJECT_FACTORY.createSmrtMtrMtrRdngDocERPBulkCrteConfMsg();
        bulkConfirmationMessage.setMessageHeader(createHeader(requestMessage, now));
        bulkConfirmationMessage.setLog(OBJECT_FACTORY.createLog());
        bulkConfirmationMessage.getLog().getItem().add(createLogItem(messageSeeds));
        return bulkConfirmationMessage;
    }

    private BusinessDocumentMessageHeader createHeader(MeterReadingDocumentCreateRequestMessage requestMessage, Instant now) {
        BusinessDocumentMessageID id = OBJECT_FACTORY.createBusinessDocumentMessageID();
        id.setValue(requestMessage.getId());

        BusinessDocumentMessageHeader messageHeader = OBJECT_FACTORY.createBusinessDocumentMessageHeader();
        messageHeader.setReferenceID(id);
        messageHeader.setCreationDateTime(now);

        return messageHeader;
    }

    private SmrtMtrMtrRdngDocERPCrteConfMsg createBody(MeterReadingDocumentCreateMessage message) {
        MeterReadingDocumentID meterReadingDocumentID = OBJECT_FACTORY.createMeterReadingDocumentID();
        meterReadingDocumentID.setValue(message.getId());

        SmrtMtrMtrRdngDocERPCrteConfMtrRdngDoc document = OBJECT_FACTORY.createSmrtMtrMtrRdngDocERPCrteConfMtrRdngDoc();
        document.setID(meterReadingDocumentID);

        SmrtMtrMtrRdngDocERPCrteConfMsg confirmationMessage = OBJECT_FACTORY.createSmrtMtrMtrRdngDocERPCrteConfMsg();
        confirmationMessage.setMeterReadingDocument(document);

        return confirmationMessage;
    }

    private void createAndValidateBody(SmrtMtrMtrRdngDocERPBulkCrteConfMsg bulkConfirmationMessage,
                                       List<MeterReadingDocumentCreateMessage> messages) {
        SmrtMtrMtrRdngDocERPCrteConfMtrRdngDoc meterReadingDocument =
                OBJECT_FACTORY.createSmrtMtrMtrRdngDocERPCrteConfMtrRdngDoc();

        messages.forEach(message -> {
            if (!message.isValid()) {
                bulkConfirmationMessage.getLog()
                        .getItem()
                        .add(createLogItem(MessageSeeds.INVALID_METER_READING_DOCUMENT, message.getId()));
            } else if (!message.isBulkSupported()) {
                bulkConfirmationMessage.getLog()
                        .getItem()
                        .add(createLogItem(MessageSeeds.UNSUPPORTED_REASON_CODE, message.getId()));
            } else {
                bulkConfirmationMessage.getSmartMeterMeterReadingDocumentERPCreateConfirmationMessage()
                        .add(createBody(message));
            }
        });
    }

    private LogItem createLogItem(MessageSeeds messageSeeds, Object... args) {
        LogItemCategoryCode logItemCategoryCode = OBJECT_FACTORY.createLogItemCategoryCode();
        logItemCategoryCode.setValue(PROCESSING_ERROR_CATEGORY_CODE);

        LogItem logItem = OBJECT_FACTORY.createLogItem();
        logItem.setTypeID(String.valueOf(messageSeeds.getNumber()));
        logItem.setCategoryCode(logItemCategoryCode);
        logItem.setNote(messageSeeds.getDefaultFormat(args));

        return logItem;
    }
}