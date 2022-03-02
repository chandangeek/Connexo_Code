/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreplacement.meterchange;

import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangeconfirmation.UtilsDvceERPSmrtMtrChgConfMsg;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class UtilitiesDeviceMeterChangeConfirmationMessage {

    private static final MeterChangeConfirmationMessageFactory SINGLE_MESSAGE_FACTORY = new MeterChangeConfirmationMessageFactory();

    private UtilsDvceERPSmrtMtrChgConfMsg confirmationMessage;

    public Optional<UtilsDvceERPSmrtMtrChgConfMsg> getConfirmationMessage() {
        return Optional.ofNullable(confirmationMessage);
    }

    public static Builder builder() {
        return new UtilitiesDeviceMeterChangeConfirmationMessage().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public Builder from(ServiceCall parent, List<ServiceCall> children, String senderBusinessSystemId, Instant now) {
            confirmationMessage = SINGLE_MESSAGE_FACTORY.createMessage(parent, children.get(0), senderBusinessSystemId, now);
            return this;
        }

        public Builder from(UtilitiesDeviceMeterChangeRequestMessage message, MessageSeeds messageSeed, String senderBusinessSystemId, Instant now, Object... messageSeedArgs) {
            confirmationMessage = SINGLE_MESSAGE_FACTORY.createMessage(message, messageSeed, senderBusinessSystemId, now, messageSeedArgs);
            return this;
        }

        public UtilitiesDeviceMeterChangeConfirmationMessage build() {
            return UtilitiesDeviceMeterChangeConfirmationMessage.this;
        }
    }
}
