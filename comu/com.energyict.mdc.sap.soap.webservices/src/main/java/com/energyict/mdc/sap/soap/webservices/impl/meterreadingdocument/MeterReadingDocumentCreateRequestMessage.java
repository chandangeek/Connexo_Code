/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreaterequest.SmrtMtrMtrRdngDocERPBulkCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.SmrtMtrMtrRdngDocERPCrteReqMsg;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MeterReadingDocumentCreateRequestMessage {

    private boolean bulk;
    private BigDecimal attemptNumber = BigDecimal.ZERO;
    private String id;
    private List<MeterReadingDocumentCreateMessage> meterReadingDocumentCreateMessages = new ArrayList<>();

    private MeterReadingDocumentCreateRequestMessage() {
    }

    public String getId() {
        return id;
    }

    public BigDecimal getAttemptNumber() {
        return attemptNumber;
    }

    public List<MeterReadingDocumentCreateMessage> getMeterReadingDocumentCreateMessages() {
        return meterReadingDocumentCreateMessages;
    }

    public boolean isValid() {
        return id != null;
    }

    public boolean isBulk() {
        return bulk;
    }

    static MeterReadingDocumentCreateRequestMessage.Builder builder() {
        return new MeterReadingDocumentCreateRequestMessage().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public MeterReadingDocumentCreateRequestMessage.Builder from(SmrtMtrMtrRdngDocERPCrteReqMsg requestMessage) {
            bulk = false;
            Optional.ofNullable(requestMessage.getMessageHeader())
                    .ifPresent(messageHeader -> {
                        setId(getId(messageHeader));
                    });
            meterReadingDocumentCreateMessages.add(MeterReadingDocumentCreateMessage
                    .builder()
                    .from(requestMessage.getMeterReadingDocument())
                    .build());
            return this;
        }

        public MeterReadingDocumentCreateRequestMessage.Builder from(SmrtMtrMtrRdngDocERPBulkCrteReqMsg requestMessage) {
            bulk = true;
            Optional.ofNullable(requestMessage.getMessageHeader())
                    .ifPresent(messageHeader -> {
                        setId(getId(messageHeader));
                    });
            requestMessage.getSmartMeterMeterReadingDocumentERPCreateRequestMessage()
                    .forEach(message ->
                            meterReadingDocumentCreateMessages.add(MeterReadingDocumentCreateMessage
                                    .builder()
                                    .from(message.getMeterReadingDocument())
                                    .build()));
            return this;
        }

        public MeterReadingDocumentCreateRequestMessage.Builder setId(String id) {
            MeterReadingDocumentCreateRequestMessage.this.id = id;
            return this;
        }

        public MeterReadingDocumentCreateRequestMessage build() {
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

    }
}