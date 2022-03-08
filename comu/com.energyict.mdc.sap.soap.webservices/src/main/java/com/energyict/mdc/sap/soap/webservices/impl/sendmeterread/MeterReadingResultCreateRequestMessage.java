/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.sendmeterread;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractSapMessage;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreaterequest.SmrtMtrMtrRdngDocERPRsltCrteReqMsg;

import java.util.Optional;

public class MeterReadingResultCreateRequestMessage extends AbstractSapMessage {

    private String id;
    private String uuid;
    private MeterReadingResultCreateMessage meterReadingResultCreateMessage;
    private Thesaurus thesaurus;

    private MeterReadingResultCreateRequestMessage(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public String getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public MeterReadingResultCreateMessage getMeterReadingResultCreateMessage() {
        return meterReadingResultCreateMessage;
    }

    static MeterReadingResultCreateRequestMessage.Builder builder(Thesaurus thesaurus) {
        return new MeterReadingResultCreateRequestMessage(thesaurus).new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public MeterReadingResultCreateRequestMessage.Builder from(SmrtMtrMtrRdngDocERPRsltCrteReqMsg requestMessage) {
            Optional.ofNullable(requestMessage.getMessageHeader())
                    .ifPresent(messageHeader -> {
                        setId(getId(messageHeader));
                        setUuid(getUuid(messageHeader));
                    });
            meterReadingResultCreateMessage = MeterReadingResultCreateMessage
                    .builder()
                    .from(requestMessage.getMeterReadingDocument())
                    .build();
            ;
            addMissingFields(meterReadingResultCreateMessage.getMissingFieldsSet());
            return this;
        }


        public MeterReadingResultCreateRequestMessage.Builder setId(String id) {
            MeterReadingResultCreateRequestMessage.this.id = id;
            return this;
        }

        public MeterReadingResultCreateRequestMessage.Builder setUuid(String uuid) {
            MeterReadingResultCreateRequestMessage.this.uuid = uuid;
            return this;
        }

        public MeterReadingResultCreateRequestMessage build() {
            if (id == null && uuid == null) {
                addAtLeastOneMissingField(thesaurus, REQUEST_ID_XML_NAME, UUID_XML_NAME);
            }
            return MeterReadingResultCreateRequestMessage.this;
        }

        private String getId(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreaterequest.BusinessDocumentMessageHeader meterReadingDocument) {
            return Optional.ofNullable(meterReadingDocument.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreaterequest.BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getUuid(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreaterequest.BusinessDocumentMessageHeader meterReadingDocument) {
            return Optional.ofNullable(meterReadingDocument.getUUID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreaterequest.UUID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }
    }
}