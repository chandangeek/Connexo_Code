package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.cancellation;

import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.webservices.impl.SeverityCode;
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
import java.util.OptionalInt;
import java.util.UUID;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.PROCESSING_ERROR_CATEGORY_CODE;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.SUCCESSFUL_PROCESSING_TYPE_ID;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID;

public class CancellationBulkConfirmationMessageFactory {
    private static final ObjectFactory objectFactory = new ObjectFactory();

    public CancellationBulkConfirmationMessageFactory() {
    }

    public SmrtMtrMtrRdngDocERPBulkCanclnConfMsg createMessage(String requestId, String uuid, List<CancelledMeterReadingDocument> documents, Instant now, String senderBusinessSystemId) {
        SmrtMtrMtrRdngDocERPBulkCanclnConfMsg confirmMsg = objectFactory.createSmrtMtrMtrRdngDocERPBulkCanclnConfMsg();
        confirmMsg.setMessageHeader(createMessageHeader(requestId, uuid, now, senderBusinessSystemId));
        createBody(confirmMsg, documents, now, senderBusinessSystemId);
        if (hasAllSuccessDocument(documents)) {
            confirmMsg.setLog(createSuccessfulLog());
        } else if (hasAllFailedDocument(documents)) {
            confirmMsg.setLog(createFailedLog(MessageSeeds.BULK_REQUEST_WAS_FAILED));
        } else {
            confirmMsg.setLog(createPartiallySuccessfulLog());
        }
        return confirmMsg;
    }

    /* Construct failed message */
    public SmrtMtrMtrRdngDocERPBulkCanclnConfMsg createMessage(MeterReadingDocumentCancellationRequestMessage requestMessage, MessageSeeds messageSeed, Instant now, String senderBusinessSystemId, Object... messageSeedArgs) {
        SmrtMtrMtrRdngDocERPBulkCanclnConfMsg bulkConfirmationMessage = objectFactory.createSmrtMtrMtrRdngDocERPBulkCanclnConfMsg();
        bulkConfirmationMessage.setMessageHeader(createMessageHeader(requestMessage.getRequestID(), requestMessage.getUuid(), now, senderBusinessSystemId));
        Log log = createLog(messageSeed,
                PROCESSING_ERROR_CATEGORY_CODE,
                UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID,
                ProcessingResultCode.FAILED.getCode(), messageSeedArgs);
        bulkConfirmationMessage.setLog(log);
        return bulkConfirmationMessage;
    }

    private boolean hasAllSuccessDocument(List<CancelledMeterReadingDocument> documents) {
        return documents.stream().allMatch(doc -> doc.isSuccess());
    }

    private boolean hasAllFailedDocument(List<CancelledMeterReadingDocument> documents) {
        return documents.stream().allMatch(doc -> !doc.isSuccess());
    }

    private void createBody(SmrtMtrMtrRdngDocERPBulkCanclnConfMsg confirmMsg, List<CancelledMeterReadingDocument> documents, Instant now, String senderBusinessSystemId) {
        documents.stream()
                .forEach(child -> confirmMsg.getSmartMeterMeterReadingDocumentERPCancellationConfirmationMessage().add(createChildMessage(child, now, senderBusinessSystemId)));
    }

    private SmrtMtrMtrRdngDocERPCanclnConfMsg createChildMessage(CancelledMeterReadingDocument document, Instant now, String senderBusinessSystemId) {
        SmrtMtrMtrRdngDocERPCanclnConfMsg confirmationMessage = objectFactory.createSmrtMtrMtrRdngDocERPCanclnConfMsg();
        confirmationMessage.setMessageHeader(createChildHeader(now, senderBusinessSystemId));
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
        return createLog(MessageSeeds.OK_RESULT,
                null,
                SUCCESSFUL_PROCESSING_TYPE_ID,
                ProcessingResultCode.SUCCESSFUL.getCode());
    }

    private Log createFailedLog(MessageSeeds messageSeeds, Object... args) {
        return createLog(messageSeeds,
                PROCESSING_ERROR_CATEGORY_CODE,
                UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID,
                ProcessingResultCode.FAILED.getCode(), args);
    }

    private Log createPartiallySuccessfulLog() {
        return createLog(MessageSeeds.PARTIALLY_SUCCESSFUL,
                PROCESSING_ERROR_CATEGORY_CODE,
                UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID,
                ProcessingResultCode.PARTIALLY_SUCCESSFUL.getCode());
    }

    private Log createLog(MessageSeeds messageSeeds, String categoryCode, String typeId, String docProcResultCode, Object... args) {

        LogItem logItem = objectFactory.createLogItem();

        if (!Strings.isNullOrEmpty(categoryCode)) {
            LogItemCategoryCode logItemCategoryCode = objectFactory.createLogItemCategoryCode();
            logItemCategoryCode.setValue(categoryCode);
            logItem.setCategoryCode(logItemCategoryCode);
        }

        logItem.setTypeID(typeId);
        logItem.setSeverityCode(SeverityCode.getSeverityCode(messageSeeds.getLevel()));

        logItem.setNote(messageSeeds.getDefaultFormat(args));

        Log log = objectFactory.createLog();
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

    private BusinessDocumentMessageHeader createChildHeader(Instant now, String senderBusinessSystemId) {
        BusinessDocumentMessageHeader header = objectFactory.createBusinessDocumentMessageHeader();
        String uuid = java.util.UUID.randomUUID().toString();

        header.setUUID(createUUID(uuid));
        header.setCreationDateTime(now);
        header.setSenderBusinessSystemID(senderBusinessSystemId);
        header.setReconciliationIndicator(true);
        return header;
    }

    private BusinessDocumentMessageHeader createMessageHeader(String requestId, String referenceUuid, Instant now, String senderBusinessSystemId) {

        String uuid = UUID.randomUUID().toString();

        BusinessDocumentMessageHeader header = objectFactory.createBusinessDocumentMessageHeader();
        if (!Strings.isNullOrEmpty(requestId)) {
            header.setReferenceID(createID(requestId));
        }
        header.setUUID(createUUID(uuid));
        if (!Strings.isNullOrEmpty(referenceUuid)) {
            header.setReferenceUUID(createUUID(referenceUuid));
        }
        header.setSenderBusinessSystemID(senderBusinessSystemId);
        header.setReconciliationIndicator(true);
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
