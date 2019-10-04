/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.cancellation;

import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.SmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg;

import java.time.Instant;
import java.util.Optional;

public class StatusChangeRequestCancellationConfirmationMessage {
    private static final CancellationConfirmationMessageFactory MESSAGE_FACTORY = new CancellationConfirmationMessageFactory();

    private SmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg confirmationMessage;

    public Optional<SmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg> getConfirmationMessage() {
        return Optional.ofNullable(confirmationMessage);
    }

    public static Builder builder() {
        return new StatusChangeRequestCancellationConfirmationMessage().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public Builder from(String requestId, CancelledStatusChangeRequestDocument document, Instant now) {
            confirmationMessage = MESSAGE_FACTORY.createMessage(requestId, document, now);
            return this;
        }

        public Builder from(StatusChangeRequestCancellationRequestMessage requestMessage, MessageSeeds messageSeed, Instant now) {
            confirmationMessage = MESSAGE_FACTORY.createMessage(requestMessage, messageSeed, now);
            return this;
        }

        public StatusChangeRequestCancellationConfirmationMessage build() {
            return StatusChangeRequestCancellationConfirmationMessage.this;
        }
    }}
