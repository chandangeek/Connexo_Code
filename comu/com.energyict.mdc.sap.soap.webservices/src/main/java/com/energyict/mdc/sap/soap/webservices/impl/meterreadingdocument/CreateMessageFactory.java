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
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MasterMeterReadingDocumentCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateRequestDomainExtension;
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

    CreateMessageFactory() {
    }

    public SmrtMtrMtrRdngDocERPCrteConfMsg createMessage(MasterMeterReadingDocumentCreateRequestDomainExtension extension, ServiceCall childServiceCall, Instant now, String senderBusinessSystemId) {
        SmrtMtrMtrRdngDocERPCrteConfMsg confirmationMessage = OBJECT_FACTORY.createSmrtMtrMtrRdngDocERPCrteConfMsg();
        confirmationMessage.setMessageHeader(createHeader(extension.getRequestID(), extension.getUuid(), now, senderBusinessSystemId));

        MeterReadingDocumentCreateRequestDomainExtension extensionChild = childServiceCall.getExtensionFor(new MeterReadingDocumentCreateRequestCustomPropertySet()).get();

        if (childServiceCall.getState().equals(DefaultState.SUCCESSFUL)) {
            confirmationMessage.setLog(createSuccessfulLog());
        } else if (childServiceCall.getState().equals(DefaultState.FAILED) || childServiceCall.getState().equals(DefaultState.CANCELLED)) {
            confirmationMessage.setLog(createFailedLog(extensionChild.getErrorMessage()));
        }
        confirmationMessage.setMeterReadingDocument(createBody(extensionChild.getMeterReadingDocumentId()));

        return confirmationMessage;
    }

    public SmrtMtrMtrRdngDocERPCrteConfMsg createMessage(MeterReadingDocumentCreateRequestMessage requestMessage,
                                                         MessageSeeds messageSeeds,
                                                         Instant now,
                                                         String senderBusinessSystemId) {
        SmrtMtrMtrRdngDocERPCrteConfMsg confirmationMessage = OBJECT_FACTORY.createSmrtMtrMtrRdngDocERPCrteConfMsg();

        confirmationMessage.setMessageHeader(createHeader(requestMessage.getId(), requestMessage.getUuid(), now, senderBusinessSystemId));
        confirmationMessage.setLog(createFailedLog(messageSeeds.getDefaultFormat()));
        requestMessage.getMeterReadingDocumentCreateMessages()
                .forEach(message -> confirmationMessage.setMeterReadingDocument(createBody(message.getId())));
        return confirmationMessage;
    }

    private BusinessDocumentMessageHeader createHeader(String requestId, String requestUuid, Instant now, String senderBusinessSystemId) {

        BusinessDocumentMessageHeader messageHeader = OBJECT_FACTORY.createBusinessDocumentMessageHeader();
        String uuid = java.util.UUID.randomUUID().toString();

        if (!Strings.isNullOrEmpty(requestId)) {
            BusinessDocumentMessageID messageID = OBJECT_FACTORY.createBusinessDocumentMessageID();
            messageID.setValue(requestId);
            messageHeader.setReferenceID(messageID);
        }

        messageHeader.setUUID(createUUID(uuid));
        if (!Strings.isNullOrEmpty(requestUuid)) {
            messageHeader.setReferenceUUID(createUUID(requestUuid));
        }

        messageHeader.setCreationDateTime(now);
        messageHeader.setSenderBusinessSystemID(senderBusinessSystemId);
        messageHeader.setReconciliationIndicator(true);


        return messageHeader;
    }

    private UUID createUUID(String uuid) {
        UUID messageUUID = OBJECT_FACTORY.createUUID();
        messageUUID.setValue(uuid);
        return messageUUID;
    }

    private SmrtMtrMtrRdngDocERPCrteConfMtrRdngDoc createBody(String meterReadingDocumentId) {
        MeterReadingDocumentID meterReadingDocumentID = OBJECT_FACTORY.createMeterReadingDocumentID();
        meterReadingDocumentID.setValue(meterReadingDocumentId);

        SmrtMtrMtrRdngDocERPCrteConfMtrRdngDoc meterReadingDocument = OBJECT_FACTORY.createSmrtMtrMtrRdngDocERPCrteConfMtrRdngDoc();
        meterReadingDocument.setID(meterReadingDocumentID);

        return meterReadingDocument;
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