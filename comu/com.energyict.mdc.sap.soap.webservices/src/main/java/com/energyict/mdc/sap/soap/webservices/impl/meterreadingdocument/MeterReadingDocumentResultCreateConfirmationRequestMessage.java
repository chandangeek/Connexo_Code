/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreateconfirmation.MtrRdngDocERPRsltBulkCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreateconfirmation.MtrRdngDocERPRsltCrteConfMsg;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MeterReadingDocumentResultCreateConfirmationRequestMessage {

    private boolean bulk;
    private String id;
    private String uuid;

    //<meter reading document id, result code>
    Map<String, String> processingResultCodes = new HashMap<>();

    private MeterReadingDocumentResultCreateConfirmationRequestMessage() {
    }

    public String getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public Map<String, String> getProcessingResultCodes() {
        return processingResultCodes;
    }

    public boolean isBulk() {
        return bulk;
    }

    public static MeterReadingDocumentResultCreateConfirmationRequestMessage.Builder builder() {
        return new MeterReadingDocumentResultCreateConfirmationRequestMessage().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public Builder from(MtrRdngDocERPRsltCrteConfMsg requestMessage) {
            bulk = false;
            Optional.ofNullable(requestMessage.getMessageHeader())
                    .ifPresent(messageHeader -> {
                        setId(getId(messageHeader));
                        setUUID(getUUID(messageHeader));
                    });

            addProcessingResultCode(getMeterReadingDocumentId(requestMessage), getProcessingResultCode(requestMessage));
            return this;
        }

        public Builder from(MtrRdngDocERPRsltBulkCrteConfMsg requestMessage) {
            bulk = true;
            Optional.ofNullable(requestMessage.getMessageHeader())
                    .ifPresent(messageHeader -> {
                        setId(getId(messageHeader));
                        setUUID(getUUID(messageHeader));
                    });

            requestMessage.getMeterReadingDocumentERPResultCreateConfirmationMessage()
                    .forEach(message->addProcessingResultCode(getMeterReadingDocumentId(message), getProcessingResultCode(message)));

            return this;
        }

        public MeterReadingDocumentResultCreateConfirmationRequestMessage.Builder setUUID(String uuid) {
            MeterReadingDocumentResultCreateConfirmationRequestMessage.this.uuid = uuid;
            return this;
        }

        public MeterReadingDocumentResultCreateConfirmationRequestMessage.Builder setId(String id) {
            MeterReadingDocumentResultCreateConfirmationRequestMessage.this.id = id;
            return this;
        }


        public MeterReadingDocumentResultCreateConfirmationRequestMessage.Builder addProcessingResultCode(String meterReadingDocumentId, String processingResultCode) {
            MeterReadingDocumentResultCreateConfirmationRequestMessage.this.processingResultCodes.put(meterReadingDocumentId, processingResultCode);
            return this;
        }

        public MeterReadingDocumentResultCreateConfirmationRequestMessage build() {
            return MeterReadingDocumentResultCreateConfirmationRequestMessage.this;
        }

        private String getId(com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreateconfirmation.BusinessDocumentMessageHeader meterReadingDocument) {
            return Optional.ofNullable(meterReadingDocument.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreateconfirmation.BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getId(com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreateconfirmation.BusinessDocumentMessageHeader meterReadingDocument) {
            return Optional.ofNullable(meterReadingDocument.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreateconfirmation.BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getMeterReadingDocumentId(MtrRdngDocERPRsltCrteConfMsg msg) {
            return Optional.ofNullable(msg.getMeterReadingDocument())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreateconfirmation.MtrRdngDocERPRsltCrteConfMtrRdngDoc::getID)
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreateconfirmation.MeterReadingDocumentID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getMeterReadingDocumentId(com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreateconfirmation.MtrRdngDocERPRsltCrteConfMsg msg) {
            return Optional.ofNullable(msg.getMeterReadingDocument())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreateconfirmation.MtrRdngDocERPRsltCrteConfMtrRdngDoc::getID)
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreateconfirmation.MeterReadingDocumentID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getProcessingResultCode(MtrRdngDocERPRsltCrteConfMsg msg) {
            return Optional.ofNullable(msg.getLog())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreateconfirmation.Log::getBusinessDocumentProcessingResultCode)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        public String getProcessingResultCode(com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreateconfirmation.MtrRdngDocERPRsltCrteConfMsg msg) {
            return Optional.ofNullable(msg.getLog())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreateconfirmation.Log::getBusinessDocumentProcessingResultCode)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getUUID(com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreateconfirmation.BusinessDocumentMessageHeader meterReadingDocument) {
            return Optional.ofNullable(meterReadingDocument.getReferenceUUID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreateconfirmation.UUID::getValue)
                    .filter(referenceId -> !Checks.is(referenceId).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getUUID(com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreateconfirmation.BusinessDocumentMessageHeader meterReadingDocument) {
            return Optional.ofNullable(meterReadingDocument.getReferenceUUID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreateconfirmation.UUID::getValue)
                    .filter(referenceId -> !Checks.is(referenceId).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }
    }
}