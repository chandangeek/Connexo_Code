package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.cancellation;

import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.webservices.impl.SeverityCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.LogItemCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.MeterReadingDocumentID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.SmrtMtrMtrRdngDocERPCanclnConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.SmrtMtrMtrRdngDocERPCanclnConfMtrRdngDoc;

import com.google.common.base.Strings;

import java.time.Instant;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.logging.Level;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.PROCESSING_ERROR_CATEGORY_CODE;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.SUCCESSFUL_PROCESSING_TYPE_ID;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID;

public class CancellationConfirmationMessageFactory {
    private static final ObjectFactory objectFactory = new ObjectFactory();

    public CancellationConfirmationMessageFactory() {
    }

    public SmrtMtrMtrRdngDocERPCanclnConfMsg createMessage(String requestId, String uuid, CancelledMeterReadingDocument document, Instant now, String senderBusinessSystemId) {
        SmrtMtrMtrRdngDocERPCanclnConfMsg confirmMsg = objectFactory.createSmrtMtrMtrRdngDocERPCanclnConfMsg();
        confirmMsg.setMessageHeader(createMessageHeader(requestId, uuid, now, senderBusinessSystemId));
        if (document.isSuccess()) {
            confirmMsg.setLog(createSuccessfulLog());
        } else {
            confirmMsg.setLog(createFailedLog(document.getCancelError(), document.getCancelArgs()));
        }
        confirmMsg.setMeterReadingDocument(createBodyMessage(document.getId()));

        return confirmMsg;
    }

    public SmrtMtrMtrRdngDocERPCanclnConfMsg createMessage(MeterReadingDocumentCancellationRequestMessage requestMessage, MessageSeeds messageSeed, Instant now, String senderBusinessSystemId, Object... messageSeedArgs) {
        SmrtMtrMtrRdngDocERPCanclnConfMsg bulkConfirmationMessage = objectFactory.createSmrtMtrMtrRdngDocERPCanclnConfMsg();
        bulkConfirmationMessage.setMessageHeader(createMessageHeader(requestMessage.getRequestID(), requestMessage.getUuid(), now, senderBusinessSystemId));
        bulkConfirmationMessage.setLog(objectFactory.createLog());
        bulkConfirmationMessage.getLog().getItem().add(createLogItem(messageSeed, PROCESSING_ERROR_CATEGORY_CODE, UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID, messageSeedArgs));
        return bulkConfirmationMessage;
    }

    private SmrtMtrMtrRdngDocERPCanclnConfMtrRdngDoc createBodyMessage(String mrId) {
        SmrtMtrMtrRdngDocERPCanclnConfMtrRdngDoc confirmationMessage = objectFactory.createSmrtMtrMtrRdngDocERPCanclnConfMtrRdngDoc();
        MeterReadingDocumentID valueId = objectFactory.createMeterReadingDocumentID();
        valueId.setValue(mrId);
        confirmationMessage.setID(valueId);

        return confirmationMessage;
    }

    private Log createSuccessfulLog() {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.SUCCESSFUL.getCode());
        log.getItem().add(createLogItem(MessageSeeds.OK_RESULT, null, SUCCESSFUL_PROCESSING_TYPE_ID));
        setMaximumLogItemSeverityCode(log);
        return log;
    }

    private Log createFailedLog(MessageSeeds messageSeeds, Object... args) {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
        log.getItem().add(createLogItem(messageSeeds, PROCESSING_ERROR_CATEGORY_CODE, UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID, args));
        setMaximumLogItemSeverityCode(log);

        return log;
    }


    private LogItem createLogItem(MessageSeeds messageSeeds, String categoryCode, String typeId, Object... args) {
        LogItem logItem = objectFactory.createLogItem();

        if (!Strings.isNullOrEmpty(categoryCode)) {
            LogItemCategoryCode logItemCategoryCode = objectFactory.createLogItemCategoryCode();
            logItemCategoryCode.setValue(categoryCode);
            logItem.setCategoryCode(logItemCategoryCode);
        }

        logItem.setTypeID(typeId);
        logItem.setSeverityCode(SeverityCode.getSeverityCode(messageSeeds.getLevel()));

        logItem.setNote(messageSeeds.getDefaultFormat(args));

        return logItem;
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

    private com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.UUID createUUID(String uuid) {
        com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.UUID messageUUID
                = objectFactory.createUUID();
        messageUUID.setValue(uuid);
        return messageUUID;
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
