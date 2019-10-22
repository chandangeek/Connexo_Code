/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.cancellation;

import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.LogItemCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.SmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.SmrtMtrUtilsConncnStsChgReqERPCanclnConfUtilsConncnStsChgReq;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.UtilitiesConnectionStatusChangeRequestID;

import java.time.Instant;
import java.util.UUID;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.PROCESSING_ERROR_CATEGORY_CODE;

public class CancellationConfirmationMessageFactory {
    private static final ObjectFactory objectFactory = new ObjectFactory();

    public CancellationConfirmationMessageFactory() {
    }

    public SmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg createMessage(String requestId, CancelledStatusChangeRequestDocument document, Instant now) {
        SmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg confirmMsg = objectFactory.createSmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg();
        confirmMsg.setMessageHeader(createMessageHeader(requestId, now));
        if (document.getTotalRequests() == 0) {
            confirmMsg.setLog(createFailedLog(MessageSeeds.ERROR_CANCELLING_STATUS_CHANGE_REQUEST_NO_REQUESTS, document.getId(), document.getCategoryCode()));
        } else if (document.getTotalRequests() > 0 && document.getCancelledRequests() == 0 && document.getNotCancelledRequests() == 0) {
            confirmMsg.setLog(createFailedLog(MessageSeeds.ERROR_CANCELLING_STATUS_CHANGE_REQUEST_ALREADY_PROCESSED, document.getId(), document.getCategoryCode()));
        } else if (document.getTotalRequests() == document.getCancelledRequests() && document.getNotCancelledRequests() == 0) {
            confirmMsg.setLog(createSuccessfulLog());
        } else if (document.getTotalRequests() > document.getCancelledRequests() && document.getNotCancelledRequests() > 0) {
            confirmMsg.setLog(createPartiallySuccessfulLog(MessageSeeds.ERROR_CANCELLING_STATUS_CHANGE_REQUEST_LOG, document.getCancelledRequests(), document.getTotalRequests(),  document.getNotCancelledRequests()));
        } else {
            confirmMsg.setLog(createFailedLog(MessageSeeds.ERROR_CANCELLING_STATUS_CHANGE_REQUEST_LOG, document.getCancelledRequests(), document.getTotalRequests(),  document.getNotCancelledRequests()));
        }
        confirmMsg.setUtilitiesConnectionStatusChangeRequest(createBodyMessage(document.getId()));

        return confirmMsg;
    }

    public SmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg createFailedMessage(StatusChangeRequestCancellationRequestMessage requestMessage, MessageSeeds messageSeed, Instant now) {
        SmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg confirmMsg = objectFactory.createSmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg();
        confirmMsg.setMessageHeader(createMessageHeader(requestMessage.getRequestId(), now));
        confirmMsg.setLog(objectFactory.createLog());
        confirmMsg.getLog().getItem().add(createLogItem(messageSeed));
        return confirmMsg;
    }

    public SmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg createFailedMessage(StatusChangeRequestCancellationRequestMessage requestMessage, String message, int number, Instant now) {
        SmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg confirmMsg = objectFactory.createSmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg();
        confirmMsg.setMessageHeader(createMessageHeader(requestMessage.getRequestId(), now));
        confirmMsg.setLog(objectFactory.createLog());
        confirmMsg.getLog().getItem().add(createLogItem(message, number));
        return confirmMsg;
    }

    private SmrtMtrUtilsConncnStsChgReqERPCanclnConfUtilsConncnStsChgReq createBodyMessage(String mrId) {
        SmrtMtrUtilsConncnStsChgReqERPCanclnConfUtilsConncnStsChgReq confirmationMessage = objectFactory.createSmrtMtrUtilsConncnStsChgReqERPCanclnConfUtilsConncnStsChgReq();
        UtilitiesConnectionStatusChangeRequestID valueId = objectFactory.createUtilitiesConnectionStatusChangeRequestID();
        valueId.setValue(mrId);
        confirmationMessage.setID(valueId);

        return confirmationMessage;
    }

    private Log createSuccessfulLog() {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.SUCCESSFUL.getCode());
        return log;
    }

    private Log createPartiallySuccessfulLog(MessageSeeds messageSeeds, Object... args) {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.PARTIALLY_SUCCESSFUL.getCode());
        log.getItem().add(createLogItem(messageSeeds, args));
        return log;
    }

    private Log createFailedLog(MessageSeeds messageSeeds, Object... args) {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
        log.getItem().add(createLogItem(messageSeeds, args));
        return log;
    }

    private LogItem createLogItem(String message, int number) {
        LogItemCategoryCode logItemCategoryCode = objectFactory.createLogItemCategoryCode();
        logItemCategoryCode.setValue(PROCESSING_ERROR_CATEGORY_CODE);

        LogItem logItem = objectFactory.createLogItem();
        logItem.setTypeID(String.valueOf(number));
        logItem.setCategoryCode(logItemCategoryCode);
        logItem.setNote(message);

        return logItem;
    }

    private LogItem createLogItem(MessageSeeds messageSeeds, Object... args) {
        LogItemCategoryCode logItemCategoryCode = objectFactory.createLogItemCategoryCode();
        logItemCategoryCode.setValue(PROCESSING_ERROR_CATEGORY_CODE);

        LogItem logItem = objectFactory.createLogItem();
        logItem.setTypeID(String.valueOf(messageSeeds.getNumber()));
        logItem.setCategoryCode(logItemCategoryCode);
        logItem.setNote(messageSeeds.getDefaultFormat(args));

        return logItem;
    }

    private BusinessDocumentMessageHeader createMessageHeader(String requestId, Instant now) {

        String uuid = UUID.randomUUID().toString();

        BusinessDocumentMessageHeader header = objectFactory.createBusinessDocumentMessageHeader();
        header.setReferenceID(createID(requestId));
        header.setUUID(createUUID(uuid));
        header.setCreationDateTime(now);
        return header;
    }

    private BusinessDocumentMessageID createID(String id) {
        BusinessDocumentMessageID messageID = objectFactory.createBusinessDocumentMessageID();
        messageID.setValue(id);
        return messageID;
    }

    private com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.UUID createUUID(String uuid) {
        com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.UUID messageUUID
                = objectFactory.createUUID();
        messageUUID.setValue(uuid);
        return messageUUID;
    }
}
