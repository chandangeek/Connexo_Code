/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

public class MeterReadingDocumentResultCreateConfirmationMessage {

    private MeterReadingDocumentResultCreateConfirmationMessage() {
    }

    public static MeterReadingDocumentResultCreateConfirmationMessage.Builder builder() {
        return new MeterReadingDocumentResultCreateConfirmationMessage().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public Builder from() {
            return this;
        }

        public MeterReadingDocumentResultCreateConfirmationMessage build() {
            return MeterReadingDocumentResultCreateConfirmationMessage.this;
        }
    }
}