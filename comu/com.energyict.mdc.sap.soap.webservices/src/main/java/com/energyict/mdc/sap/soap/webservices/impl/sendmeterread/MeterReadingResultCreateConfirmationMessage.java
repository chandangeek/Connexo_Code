/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.sendmeterread;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.webservices.impl.SeverityCode;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.receivemeterreadings.MasterMeterReadingResultCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.receivemeterreadings.MasterMeterReadingResultCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.receivemeterreadings.MeterReadingResultCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.receivemeterreadings.MeterReadingResultCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreateconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreateconfirmation.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreateconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreateconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreateconfirmation.LogItemCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreateconfirmation.MeterReadingDocumentID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreateconfirmation.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreateconfirmation.SmrtMtrMtrRdngDocERPRsltCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreateconfirmation.SmrtMtrMtrRdngDocERPRsltCrteConfMtrRdngDoc;

import com.google.common.base.Strings;

import java.time.Instant;
import java.util.OptionalInt;
import java.util.UUID;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.PROCESSING_ERROR_CATEGORY_CODE;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.SUCCESSFUL_PROCESSING_TYPE_ID;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID;

public class MeterReadingResultCreateConfirmationMessage {
    private final ObjectFactory objectFactory = new ObjectFactory();
    private SmrtMtrMtrRdngDocERPRsltCrteConfMsg confirmationMessage;

    public SmrtMtrMtrRdngDocERPRsltCrteConfMsg getConfirmationMessage() {
        return confirmationMessage;
    }

    public static MeterReadingResultCreateConfirmationMessage.Builder builder() {
        return new MeterReadingResultCreateConfirmationMessage().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public MeterReadingResultCreateConfirmationMessage.Builder from(ServiceCall parent, String senderBusinessSystemId, Instant now) {
            MasterMeterReadingResultCreateRequestDomainExtension extension = parent.getExtensionFor(new MasterMeterReadingResultCreateRequestCustomPropertySet()).get();
            ServiceCall child = parent.findChildren().stream().findFirst().orElseThrow(() -> new IllegalStateException("Unable to get child service call"));
            confirmationMessage = objectFactory.createSmrtMtrMtrRdngDocERPRsltCrteConfMsg();
            confirmationMessage.setMessageHeader(createMessageHeader(extension.getRequestId(), extension.getUuid(), senderBusinessSystemId, now));
            createBody(confirmationMessage, child, senderBusinessSystemId, now);
            return this;
        }

        public MeterReadingResultCreateConfirmationMessage.Builder from(MeterReadingResultCreateRequestMessage message, MessageSeeds messageSeed, String senderBusinessSystemId, Instant now, Object... messageSeedArgs) {
            confirmationMessage = objectFactory.createSmrtMtrMtrRdngDocERPRsltCrteConfMsg();
            confirmationMessage.setMessageHeader(createMessageHeader(message.getId(), message.getUuid(), senderBusinessSystemId, now));
            confirmationMessage.setMeterReadingDocument(createChildBody(message.getMeterReadingResultCreateMessage().getId()));
            confirmationMessage.setLog(createFailedLog(messageSeed.getDefaultFormat(messageSeedArgs)));
            return this;
        }

        public MeterReadingResultCreateConfirmationMessage build() {
            return MeterReadingResultCreateConfirmationMessage.this;
        }

        private void createBody(SmrtMtrMtrRdngDocERPRsltCrteConfMsg confirmationMessage, ServiceCall child, String senderBusinessSystemId, Instant now) {
            MeterReadingResultCreateRequestDomainExtension extension = child.getExtensionFor(new MeterReadingResultCreateRequestCustomPropertySet())
                    .orElseThrow(() -> new IllegalStateException("Couldn't find domain extension for service call " + child.getNumber()));

            confirmationMessage.setMeterReadingDocument(createChildBody(extension.getMeterReadingDocumentId()));
            if (child.getState() == DefaultState.SUCCESSFUL) {
                confirmationMessage.setLog(createSuccessfulLog());
            } else if (child.getState() == DefaultState.FAILED || child.getState() == DefaultState.CANCELLED) {
                if (!extension.getErrorMessage().isEmpty()) {
                    confirmationMessage.setLog(createFailedLog(extension.getErrorMessage()));
                } else {
                    confirmationMessage.setLog(createFailedLog(MessageSeeds.REQUEST_WAS_FAILED.getDefaultFormat()));
                }
            } else {
                confirmationMessage.setLog(createFailedLog(MessageSeeds.UNEXPECTED_SERVICE_CALL_STATE.getDefaultFormat(child.getState())));
            }
        }

        private SmrtMtrMtrRdngDocERPRsltCrteConfMtrRdngDoc createChildBody(String id) {

            SmrtMtrMtrRdngDocERPRsltCrteConfMtrRdngDoc doc = objectFactory.createSmrtMtrMtrRdngDocERPRsltCrteConfMtrRdngDoc();
            MeterReadingDocumentID meterReadingDocumentID = new MeterReadingDocumentID();
            meterReadingDocumentID.setValue(id);
            doc.setID(meterReadingDocumentID);

            return doc;
        }

        private BusinessDocumentMessageHeader createMessageHeader(String requestId, String referenceUuid, String senderBusinessSystemId, Instant now) {
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

        private com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreateconfirmation.UUID createUUID(String uuid) {
            com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreateconfirmation.UUID messageUUID
                    = objectFactory.createUUID();
            messageUUID.setValue(uuid);
            return messageUUID;
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
    }
}
