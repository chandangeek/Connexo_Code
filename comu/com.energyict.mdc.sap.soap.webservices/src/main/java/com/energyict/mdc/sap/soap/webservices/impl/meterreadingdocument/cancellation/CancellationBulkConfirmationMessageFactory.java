package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.cancellation;

import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationconfirmation.LogItemCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationconfirmation.MeterReadingDocumentID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationconfirmation.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationconfirmation.SmrtMtrMtrRdngDocERPBulkCanclnConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationconfirmation.SmrtMtrMtrRdngDocERPCanclnConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationconfirmation.SmrtMtrMtrRdngDocERPCanclnConfMtrRdngDoc;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationconfirmation.BusinessDocumentMessageID;

import com.google.common.base.Strings;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.PROCESSING_ERROR_CATEGORY_CODE;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID;

public class CancellationBulkConfirmationMessageFactory {
    private static final ObjectFactory objectFactory = new ObjectFactory();

    public CancellationBulkConfirmationMessageFactory() {
    }

    public SmrtMtrMtrRdngDocERPBulkCanclnConfMsg createMessage(String requestId, String uuid, List<CancelledMeterReadingDocument> documents, Instant now) {
        SmrtMtrMtrRdngDocERPBulkCanclnConfMsg confirmMsg = objectFactory.createSmrtMtrMtrRdngDocERPBulkCanclnConfMsg();
        confirmMsg.setMessageHeader(createMessageHeader(requestId, uuid, now));
        createBody(confirmMsg, documents, now);
        if (hasAllSuccessDocument(documents)) {
            confirmMsg.setLog(createSuccessfulLog());
        } else if (hasAllFailedDocument(documents)) {
            confirmMsg.setLog(createFailedLog());
        } else {
            confirmMsg.setLog(createPartiallySuccessfulLog());
        }
        return confirmMsg;
    }

    public SmrtMtrMtrRdngDocERPBulkCanclnConfMsg createMessage(MeterReadingDocumentCancellationRequestMessage requestMessage, MessageSeeds messageSeed, Instant now) {
        SmrtMtrMtrRdngDocERPBulkCanclnConfMsg bulkConfirmationMessage = objectFactory.createSmrtMtrMtrRdngDocERPBulkCanclnConfMsg();
        bulkConfirmationMessage.setMessageHeader(createMessageHeader(requestMessage.getRequestID(), requestMessage.getUuid(), now));
        bulkConfirmationMessage.setLog(objectFactory.createLog());
        bulkConfirmationMessage.getLog().getItem().add(createLogItem(messageSeed));
        return bulkConfirmationMessage;
    }

    private boolean hasAllSuccessDocument(List<CancelledMeterReadingDocument> documents) {
        return documents.stream().allMatch(doc -> doc.isSuccess());
    }

    private boolean hasAllFailedDocument(List<CancelledMeterReadingDocument> documents) {
        return documents.stream().allMatch(doc -> !doc.isSuccess());
    }

    private void createBody(SmrtMtrMtrRdngDocERPBulkCanclnConfMsg confirmMsg, List<CancelledMeterReadingDocument> documents, Instant now) {
        documents.stream()
                .forEach(child ->confirmMsg.getSmartMeterMeterReadingDocumentERPCancellationConfirmationMessage().add(createChildMessage(child, now)));
    }

    private SmrtMtrMtrRdngDocERPCanclnConfMsg createChildMessage(CancelledMeterReadingDocument document, Instant now) {
        SmrtMtrMtrRdngDocERPCanclnConfMsg confirmationMessage = objectFactory.createSmrtMtrMtrRdngDocERPCanclnConfMsg();
        confirmationMessage.setMessageHeader(createChildHeader(now));
        MeterReadingDocumentID valueId = objectFactory.createMeterReadingDocumentID();
        valueId.setValue(document.getId());
        SmrtMtrMtrRdngDocERPCanclnConfMtrRdngDoc value = objectFactory.createSmrtMtrMtrRdngDocERPCanclnConfMtrRdngDoc();
        value.setID(valueId);
        confirmationMessage.setMeterReadingDocument(value);
        if (document.isSuccess()) {
            confirmationMessage.setLog(createSuccessfulLog());
        } else {
            confirmationMessage.setLog(createFailedLog(document.getCancelError(), document.getCancelArgs()));
        }
        return confirmationMessage;
    }

    private Log createSuccessfulLog() {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.SUCCESSFUL.getCode());
        return log;
    }

    private Log createFailedLog() {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
        return log;
    }

    private Log createFailedLog(MessageSeeds messageSeeds, Object... args) {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
        log.getItem().add(createLogItem(messageSeeds, args));
        return log;
    }

    private Log createPartiallySuccessfulLog() {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.PARTIALLY_SUCCESSFUL.getCode());
        return log;
    }

    private LogItem createLogItem(MessageSeeds messageSeeds, Object... args) {
        LogItemCategoryCode logItemCategoryCode = objectFactory.createLogItemCategoryCode();
        logItemCategoryCode.setValue(PROCESSING_ERROR_CATEGORY_CODE);

        LogItem logItem = objectFactory.createLogItem();
        logItem.setTypeID(UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID);
        logItem.setCategoryCode(logItemCategoryCode);
        logItem.setNote(messageSeeds.getDefaultFormat(args));

        return logItem;
    }

    private BusinessDocumentMessageHeader createChildHeader(Instant now) {
        BusinessDocumentMessageHeader header = objectFactory.createBusinessDocumentMessageHeader();

        header.setCreationDateTime(now);
        return header;
    }

    private BusinessDocumentMessageHeader createMessageHeader(String requestId, String referenceUuid, Instant now) {

        String uuid = UUID.randomUUID().toString();

        BusinessDocumentMessageHeader header = objectFactory.createBusinessDocumentMessageHeader();
        if (!Strings.isNullOrEmpty(requestId)){
            header.setReferenceID(createID(requestId));
        }
        header.setUUID(createUUID(uuid));
        if (!Strings.isNullOrEmpty(referenceUuid)) {
            header.setReferenceUUID(createUUID(referenceUuid));
        }
        header.setCreationDateTime(now);
        return header;
    }

    private BusinessDocumentMessageID createID(String id) {
        BusinessDocumentMessageID messageID = objectFactory.createBusinessDocumentMessageID();
        messageID.setValue(id);
        return messageID;
    }

    private com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationconfirmation.UUID createUUID(String uuid) {
        com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationconfirmation.UUID messageUUID
                = objectFactory.createUUID();
        messageUUID.setValue(uuid);
        return messageUUID;
    }
}
