/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractSapMessage;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreaterequest.SmrtMtrMtrRdngDocERPBulkCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.SmrtMtrMtrRdngDocERPCrteReqMsg;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MeterReadingDocumentCreateRequestMessage extends AbstractSapMessage {

    private boolean bulk;
    private BigDecimal attemptNumber = BigDecimal.ZERO;
    private String id;
    private String uuid;
    private List<MeterReadingDocumentCreateMessage> meterReadingDocumentCreateMessages = new ArrayList<>();
    private Thesaurus thesaurus;

    private MeterReadingDocumentCreateRequestMessage(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public String getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public BigDecimal getAttemptNumber() {
        return attemptNumber;
    }

    public List<MeterReadingDocumentCreateMessage> getMeterReadingDocumentCreateMessages() {
        return meterReadingDocumentCreateMessages;
    }

    public boolean isBulk() {
        return bulk;
    }

    static MeterReadingDocumentCreateRequestMessage.Builder builder(Thesaurus thesaurus) {
        return new MeterReadingDocumentCreateRequestMessage(thesaurus).new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public MeterReadingDocumentCreateRequestMessage.Builder from(SmrtMtrMtrRdngDocERPCrteReqMsg requestMessage) {
            bulk = false;
            Optional.ofNullable(requestMessage.getMessageHeader())
                    .ifPresent(messageHeader -> {
                        setId(getId(messageHeader));
                        setUuid(getUuid(messageHeader));
                    });
            MeterReadingDocumentCreateMessage message = MeterReadingDocumentCreateMessage
                    .builder()
                    .from(requestMessage.getMeterReadingDocument())
                    .build();
            meterReadingDocumentCreateMessages.add(message);
            addMissingFields(message.getMissingFieldsSet());
            return this;
        }

        public MeterReadingDocumentCreateRequestMessage.Builder from(SmrtMtrMtrRdngDocERPBulkCrteReqMsg requestMessage) {
            bulk = true;
            Optional.ofNullable(requestMessage.getMessageHeader())
                    .ifPresent(messageHeader -> {
                        setId(getId(messageHeader));
                        setUuid(getUuid(messageHeader));
                    });
            requestMessage.getSmartMeterMeterReadingDocumentERPCreateRequestMessage()
                    .forEach(message -> {
                        MeterReadingDocumentCreateMessage documentCreateMessage = MeterReadingDocumentCreateMessage
                                .builder()
                                .from(message)
                                .build();
                        meterReadingDocumentCreateMessages.add(documentCreateMessage);
                        addMissingFields(documentCreateMessage.getMissingFieldsSet());
                    });
            return this;
        }

        public MeterReadingDocumentCreateRequestMessage.Builder setId(String id) {
            MeterReadingDocumentCreateRequestMessage.this.id = id;
            return this;
        }

        public MeterReadingDocumentCreateRequestMessage.Builder setUuid(String uuid) {
            MeterReadingDocumentCreateRequestMessage.this.uuid = uuid;
            return this;
        }

        public MeterReadingDocumentCreateRequestMessage build() {
            if (id == null && uuid == null) {
                addAtLeastOneMissingField(thesaurus, REQUEST_ID_XML_NAME, UUID_XML_NAME);
            }
            return MeterReadingDocumentCreateRequestMessage.this;
        }

        private String getId(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.BusinessDocumentMessageHeader meterReadingDocument) {
            return Optional.ofNullable(meterReadingDocument.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getId(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreaterequest.BusinessDocumentMessageHeader meterReadingDocument) {
            return Optional.ofNullable(meterReadingDocument.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreaterequest.BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getUuid(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.BusinessDocumentMessageHeader meterReadingDocument) {
            return Optional.ofNullable(meterReadingDocument.getUUID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.UUID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getUuid(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreaterequest.BusinessDocumentMessageHeader meterReadingDocument) {
            return Optional.ofNullable(meterReadingDocument.getUUID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreaterequest.UUID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }
    }
}