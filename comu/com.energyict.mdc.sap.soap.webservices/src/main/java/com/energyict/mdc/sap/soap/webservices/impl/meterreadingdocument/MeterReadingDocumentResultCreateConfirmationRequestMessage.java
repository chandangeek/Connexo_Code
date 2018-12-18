/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreateconfirmation.MtrRdngDocERPRsltBulkCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreateconfirmation.MtrRdngDocERPRsltCrteConfMsg;

import java.util.Optional;

public class MeterReadingDocumentResultCreateConfirmationRequestMessage {

    private boolean bulk;
    private String id;
    private String uuid;
    private String processingResultCode;

    private MeterReadingDocumentResultCreateConfirmationRequestMessage() {
    }

    public String getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public String getProcessingResultCode() {
        return processingResultCode;
    }

    public boolean isValid() {
        return id != null && uuid != null && processingResultCode != null;
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
            Optional.ofNullable(requestMessage.getLog())
                    .ifPresent(log -> setProcessingResultCode(getProcessingResultCode(log)));
            return this;
        }

        public Builder from(MtrRdngDocERPRsltBulkCrteConfMsg requestMessage) {
            bulk = true;
            Optional.ofNullable(requestMessage.getMessageHeader())
                    .ifPresent(messageHeader -> {
                        setId(getId(messageHeader));
                        setUUID(getUUID(messageHeader));
                    });
            Optional.ofNullable(requestMessage.getLog())
                    .ifPresent(log -> setProcessingResultCode(getProcessingResultCode(log)));
            return this;
        }

        public MeterReadingDocumentResultCreateConfirmationRequestMessage.Builder setId(String id) {
            MeterReadingDocumentResultCreateConfirmationRequestMessage.this.id = id;
            return this;
        }

        public MeterReadingDocumentResultCreateConfirmationRequestMessage.Builder setUUID(String uuid) {
            MeterReadingDocumentResultCreateConfirmationRequestMessage.this.uuid = uuid;
            return this;
        }

        public MeterReadingDocumentResultCreateConfirmationRequestMessage.Builder setProcessingResultCode(String processingResultCode) {
            MeterReadingDocumentResultCreateConfirmationRequestMessage.this.processingResultCode = processingResultCode;
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

        public String getProcessingResultCode(com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreateconfirmation.Log log) {
            return log.getBusinessDocumentProcessingResultCode();
        }

        public String getProcessingResultCode(com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreateconfirmation.Log log) {
            return log.getBusinessDocumentProcessingResultCode();
        }
    }
}