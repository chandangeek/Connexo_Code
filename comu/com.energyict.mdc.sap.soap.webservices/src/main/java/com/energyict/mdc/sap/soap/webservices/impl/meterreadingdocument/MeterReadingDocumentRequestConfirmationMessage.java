/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.SmrtMtrMtrRdngDocERPBulkCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.SmrtMtrMtrRdngDocERPCrteConfMsg;

public class MeterReadingDocumentRequestConfirmationMessage {

    private static final CreateBulkMessageFactory BULK_MESSAGE_FACTORY = new CreateBulkMessageFactory();
    private static final CreateMessageFactory SINGLE_MESSAGE_FACTORY = new CreateMessageFactory();

    private SmrtMtrMtrRdngDocERPBulkCrteConfMsg bulkConfirmationMessage;
    private SmrtMtrMtrRdngDocERPCrteConfMsg confirmationMessage;
    private String url;

    public SmrtMtrMtrRdngDocERPBulkCrteConfMsg getBulkConfirmationMessage() {
        return bulkConfirmationMessage;
    }

    public SmrtMtrMtrRdngDocERPCrteConfMsg getConfirmationMessage() {
        return confirmationMessage;
    }

    public String getUrl() {
        return url;
    }

    public static Builder builder() {
        return new MeterReadingDocumentRequestConfirmationMessage().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public Builder from(MeterReadingDocumentCreateRequestMessage requestMessage) {
            if (requestMessage.isBulk()) {
                bulkConfirmationMessage = BULK_MESSAGE_FACTORY.createMessage(requestMessage);
            } else {
                confirmationMessage = SINGLE_MESSAGE_FACTORY.createMessage(requestMessage);
            }
            return this;
        }

        public Builder from(MeterReadingDocumentCreateRequestMessage requestMessage, MessageSeeds messageSeed) {
            if (requestMessage.isBulk()) {
                bulkConfirmationMessage = BULK_MESSAGE_FACTORY.createMessage(requestMessage, messageSeed);
            } else {
                confirmationMessage = SINGLE_MESSAGE_FACTORY.createMessage(requestMessage, messageSeed);
            }
            return this;
        }

        public MeterReadingDocumentRequestConfirmationMessage build() {
            return MeterReadingDocumentRequestConfirmationMessage.this;
        }
    }
}