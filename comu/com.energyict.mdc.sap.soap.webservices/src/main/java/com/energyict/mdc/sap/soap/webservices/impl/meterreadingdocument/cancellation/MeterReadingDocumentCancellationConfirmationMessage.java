package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.cancellation;

import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcancellationconfirmation.SmrtMtrMtrRdngDocERPBulkCanclnConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcancellationconfirmation.SmrtMtrMtrRdngDocERPCanclnConfMsg;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class MeterReadingDocumentCancellationConfirmationMessage {

    private static final CancellationBulkConfirmationMessageFactory BULK_MESSAGE_FACTORY = new CancellationBulkConfirmationMessageFactory();
    private static final CancellationConfirmationMessageFactory SINGLE_MESSAGE_FACTORY = new CancellationConfirmationMessageFactory();

    private SmrtMtrMtrRdngDocERPBulkCanclnConfMsg bulkConfirmationMessage;
    private SmrtMtrMtrRdngDocERPCanclnConfMsg confirmationMessage;

    public Optional<SmrtMtrMtrRdngDocERPBulkCanclnConfMsg> getBulkConfirmationMessage() {
        return Optional.ofNullable(bulkConfirmationMessage);
    }

    public Optional<SmrtMtrMtrRdngDocERPCanclnConfMsg> getConfirmationMessage() {
        return Optional.ofNullable(confirmationMessage);
    }

    public static Builder builder() {
        return new MeterReadingDocumentCancellationConfirmationMessage().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public Builder from(String requestId, List<CancelledMeterReadingDocument> documents, Instant now, boolean isBulk) {
            if (isBulk) {
                bulkConfirmationMessage = BULK_MESSAGE_FACTORY.createMessage(requestId, documents, now);
            } else {
                confirmationMessage = SINGLE_MESSAGE_FACTORY.createMessage(requestId, documents.get(0), now);
            }
            return this;
        }

        public Builder from(MeterReadingDocumentCancellationRequestMessage requestMessage, MessageSeeds messageSeed, Instant now) {
            if (requestMessage.isBulk()) {
                bulkConfirmationMessage = BULK_MESSAGE_FACTORY.createMessage(requestMessage, messageSeed, now);
            } else {
                confirmationMessage = SINGLE_MESSAGE_FACTORY.createMessage(requestMessage, messageSeed, now);
            }
            return this;
        }

        public MeterReadingDocumentCancellationConfirmationMessage build() {
            return MeterReadingDocumentCancellationConfirmationMessage.this;
        }
    }
}
