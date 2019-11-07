package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.cancellation;

import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.LogItemCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.MeterReadingDocumentID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.SmrtMtrMtrRdngDocERPCanclnConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.SmrtMtrMtrRdngDocERPCanclnConfMtrRdngDoc;

import java.time.Instant;
import java.util.UUID;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.PROCESSING_ERROR_CATEGORY_CODE;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.UNSUCCESSFUL_PROCESSING_CODE;

public class CancellationConfirmationMessageFactory {
    private static final ObjectFactory objectFactory = new ObjectFactory();

    public CancellationConfirmationMessageFactory() {
    }

    public SmrtMtrMtrRdngDocERPCanclnConfMsg createMessage(String requestId, String uuid, CancelledMeterReadingDocument document, Instant now) {
        SmrtMtrMtrRdngDocERPCanclnConfMsg confirmMsg = objectFactory.createSmrtMtrMtrRdngDocERPCanclnConfMsg();
        confirmMsg.setMessageHeader(createMessageHeader(requestId, uuid, now));
        if (document.isSuccess()) {
            confirmMsg.setLog(createSuccessfulLog());
        } else {
            confirmMsg.setLog(createFailedLog(document.getCancelError(), document.getCancelArgs()));
        }
        confirmMsg.setMeterReadingDocument(createBodyMessage(document.getId()));

        return confirmMsg;
    }

    public SmrtMtrMtrRdngDocERPCanclnConfMsg createMessage(MeterReadingDocumentCancellationRequestMessage requestMessage, MessageSeeds messageSeed, Instant now) {
        SmrtMtrMtrRdngDocERPCanclnConfMsg bulkConfirmationMessage = objectFactory.createSmrtMtrMtrRdngDocERPCanclnConfMsg();
        bulkConfirmationMessage.setMessageHeader(createMessageHeader(requestMessage.getRequestID(), requestMessage.getUuid(), now));
        bulkConfirmationMessage.setLog(objectFactory.createLog());
        bulkConfirmationMessage.getLog().getItem().add(createLogItem(messageSeed));
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
        return log;
    }

    private Log createFailedLog(MessageSeeds messageSeeds, Object... args) {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
        log.getItem().add(createLogItem(messageSeeds, args));
        return log;
    }

    private LogItem createLogItem(MessageSeeds messageSeeds, Object... args) {
        LogItemCategoryCode logItemCategoryCode = objectFactory.createLogItemCategoryCode();
        logItemCategoryCode.setValue(PROCESSING_ERROR_CATEGORY_CODE);

        LogItem logItem = objectFactory.createLogItem();
        logItem.setTypeID(UNSUCCESSFUL_PROCESSING_CODE);
        logItem.setCategoryCode(logItemCategoryCode);
        logItem.setNote(messageSeeds.getDefaultFormat(args));

        return logItem;
    }


    private BusinessDocumentMessageHeader createMessageHeader(String requestId, String referenceUuid, Instant now) {

        String uuid = UUID.randomUUID().toString();

        BusinessDocumentMessageHeader header = objectFactory.createBusinessDocumentMessageHeader();
        header.setReferenceID(createID(requestId));
        header.setUUID(createUUID(uuid));
        header.setReferenceUUID(createUUID(referenceUuid));
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

}
