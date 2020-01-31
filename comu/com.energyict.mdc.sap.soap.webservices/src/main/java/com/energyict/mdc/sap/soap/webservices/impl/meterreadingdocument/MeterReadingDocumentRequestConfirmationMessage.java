/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MasterMeterReadingDocumentCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.SmrtMtrMtrRdngDocERPBulkCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.SmrtMtrMtrRdngDocERPCrteConfMsg;

import java.time.Instant;
import java.util.List;

public class MeterReadingDocumentRequestConfirmationMessage {

    private static final CreateBulkMessageFactory BULK_MESSAGE_FACTORY = new CreateBulkMessageFactory();
    private static final CreateMessageFactory SINGLE_MESSAGE_FACTORY = new CreateMessageFactory();

    private SmrtMtrMtrRdngDocERPBulkCrteConfMsg bulkConfirmationMessage;
    private SmrtMtrMtrRdngDocERPCrteConfMsg confirmationMessage;

    public SmrtMtrMtrRdngDocERPBulkCrteConfMsg getBulkConfirmationMessage() {
        return bulkConfirmationMessage;
    }

    public SmrtMtrMtrRdngDocERPCrteConfMsg getConfirmationMessage() {
        return confirmationMessage;
    }

    public static Builder builder() {
        return new MeterReadingDocumentRequestConfirmationMessage().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public Builder from(MasterMeterReadingDocumentCreateRequestDomainExtension extension, List<ServiceCall> children, Instant now, String senderBusinessSystemId) {
            if (extension.isBulk()) {
                bulkConfirmationMessage = BULK_MESSAGE_FACTORY.createMessage(extension, children, now, senderBusinessSystemId);
            } else {
                confirmationMessage = SINGLE_MESSAGE_FACTORY.createMessage(extension, children.get(0), now, senderBusinessSystemId);
            }
            return this;
        }

        public Builder from(MeterReadingDocumentCreateRequestMessage requestMessage, MessageSeeds messageSeed, Instant now, String senderBusinessSystemId, Object ...messageSeedArgs) {
            if (requestMessage.isBulk()) {
                bulkConfirmationMessage = BULK_MESSAGE_FACTORY.createMessage(requestMessage, messageSeed, now, senderBusinessSystemId, messageSeedArgs);
            } else {
                confirmationMessage = SINGLE_MESSAGE_FACTORY.createMessage(requestMessage, messageSeed, now, senderBusinessSystemId, messageSeedArgs);
            }
            return this;
        }

        public MeterReadingDocumentRequestConfirmationMessage build() {
            return MeterReadingDocumentRequestConfirmationMessage.this;
        }
    }
}