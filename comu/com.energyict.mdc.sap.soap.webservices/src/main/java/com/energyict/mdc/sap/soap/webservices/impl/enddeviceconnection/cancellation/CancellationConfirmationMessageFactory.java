/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.cancellation;

import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.webservices.impl.SeverityCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.LogItemCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.SmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.SmrtMtrUtilsConncnStsChgReqERPCanclnConfUtilsConncnStsChgReq;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.UtilitiesConnectionStatusChangeRequestCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.UtilitiesConnectionStatusChangeRequestID;

import com.google.common.base.Strings;

import java.time.Instant;
import java.util.OptionalInt;
import java.util.UUID;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.PROCESSING_ERROR_CATEGORY_CODE;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.SUCCESSFUL_PROCESSING_TYPE_ID;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID;

public class CancellationConfirmationMessageFactory {
    private static final ObjectFactory objectFactory = new ObjectFactory();

    public CancellationConfirmationMessageFactory() {
    }

    public SmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg createMessage(String requestId, String uuid, CancelledStatusChangeRequestDocument document, String senderBusinessSystemId, Instant now) {
        SmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg confirmMsg = objectFactory.createSmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg();
        confirmMsg.setMessageHeader(createMessageHeader(requestId, uuid, senderBusinessSystemId, now));
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
        confirmMsg.setUtilitiesConnectionStatusChangeRequest(createBodyMessage(document.getId(), document.getCategoryCode()));

        return confirmMsg;
    }

    public SmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg createFailedMessage(StatusChangeRequestCancellationRequestMessage requestMessage, MessageSeeds messageSeed, String senderBusinessSystemId, Instant now, Object... messageSeedArgs) {
        SmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg confirmMsg = objectFactory.createSmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg();
        confirmMsg.setMessageHeader(createMessageHeader(requestMessage.getRequestId(), requestMessage.getUuid(), senderBusinessSystemId, now));
        confirmMsg.setLog(createFailedLog(messageSeed, messageSeedArgs));
        return confirmMsg;
    }

    public SmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg createFailedMessage(StatusChangeRequestCancellationRequestMessage requestMessage, String message, String senderBusinessSystemId, Instant now) {
        SmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg confirmMsg = objectFactory.createSmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg();
        confirmMsg.setMessageHeader(createMessageHeader(requestMessage.getRequestId(), requestMessage.getUuid(), senderBusinessSystemId, now));
        confirmMsg.setLog(createFailedLog(message));
        return confirmMsg;
    }

    private SmrtMtrUtilsConncnStsChgReqERPCanclnConfUtilsConncnStsChgReq createBodyMessage(String id, String code) {
        SmrtMtrUtilsConncnStsChgReqERPCanclnConfUtilsConncnStsChgReq confirmationMessage = objectFactory.createSmrtMtrUtilsConncnStsChgReqERPCanclnConfUtilsConncnStsChgReq();
        UtilitiesConnectionStatusChangeRequestID valueId = objectFactory.createUtilitiesConnectionStatusChangeRequestID();
        valueId.setValue(id);
        confirmationMessage.setID(valueId);
        UtilitiesConnectionStatusChangeRequestCategoryCode categoryCode = objectFactory.createUtilitiesConnectionStatusChangeRequestCategoryCode();
        categoryCode.setValue(code);
        confirmationMessage.setCategoryCode(categoryCode);
        return confirmationMessage;
    }

    private Log createSuccessfulLog() {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.SUCCESSFUL.getCode());
        log.getItem().add(createLogItem(MessageSeeds.OK_RESULT.getDefaultFormat(),
                SUCCESSFUL_PROCESSING_TYPE_ID, SeverityCode.INFORMATION.getCode(),
                null));
        setMaximumLogItemSeverityCode(log);
        return log;
    }

    private Log createPartiallySuccessfulLog(MessageSeeds messageSeeds, Object... args) {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.PARTIALLY_SUCCESSFUL.getCode());
        log.getItem().add(createLogItem(MessageSeeds.PARTIALLY_SUCCESSFUL.getDefaultFormat(),
                UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID, SeverityCode.ERROR.getCode(),
                null));
        setMaximumLogItemSeverityCode(log);
        return log;
    }

    private Log createFailedLog(MessageSeeds messageSeeds, Object... args) {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
        log.getItem().add(createLogItem(messageSeeds.getDefaultFormat(args), UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID,
                SeverityCode.ERROR.getCode(), PROCESSING_ERROR_CATEGORY_CODE));
        setMaximumLogItemSeverityCode(log);
        return log;
    }

    private Log createFailedLog(String message) {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
        log.getItem().add(createLogItem(message, UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID,
                SeverityCode.ERROR.getCode(), PROCESSING_ERROR_CATEGORY_CODE));
        setMaximumLogItemSeverityCode(log);
        return log;
    }

    private LogItem createLogItem(String message, String typeId, String severityCode, String categoryCode) {
        LogItem logItem = objectFactory.createLogItem();
        if (!Strings.isNullOrEmpty(categoryCode)) {
            LogItemCategoryCode logItemCategoryCode = objectFactory.createLogItemCategoryCode();
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
            Integer value = maxInt.getAsInt();
            log.setMaximumLogItemSeverityCode(value.toString());
        }
    }
    private BusinessDocumentMessageHeader createMessageHeader(String requestId, String referenceUuid, String senderBusinessSystemId, Instant now) {

        String uuid = UUID.randomUUID().toString();

        BusinessDocumentMessageHeader header = objectFactory.createBusinessDocumentMessageHeader();
        if (!Strings.isNullOrEmpty(requestId)){
            header.setReferenceID(createID(requestId));
        }
        header.setUUID(createUUID(uuid));
        if (!Strings.isNullOrEmpty(referenceUuid)){
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

    private com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.UUID createUUID(String uuid) {
        com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.UUID messageUUID
                = objectFactory.createUUID();
        messageUUID.setValue(uuid);
        return messageUUID;
    }
}
