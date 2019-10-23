/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.UtilsDvceERPSmrtMtrBlkCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilsDvceERPSmrtMtrCrteConfMsg;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.PROCESSING_ERROR_CATEGORY_CODE;

public class UtilitiesDeviceCreateConfirmationMessage {

    private static final CreateBulkConfirmationMessageFactory BULK_MESSAGE_FACTORY = new CreateBulkConfirmationMessageFactory();
    private static final CreateConfirmationMessageFactory SINGLE_MESSAGE_FACTORY = new CreateConfirmationMessageFactory();

    private UtilsDvceERPSmrtMtrCrteConfMsg confirmationMessage;
    private UtilsDvceERPSmrtMtrBlkCrteConfMsg bulkConfirmationMessage;

    public Optional<UtilsDvceERPSmrtMtrCrteConfMsg> getConfirmationMessage() {
        return Optional.ofNullable(confirmationMessage);
    }
    public Optional<UtilsDvceERPSmrtMtrBlkCrteConfMsg> getBulkConfirmationMessage() {
        return Optional.ofNullable(bulkConfirmationMessage);
    }

    public static Builder builder() {
        return new UtilitiesDeviceCreateConfirmationMessage().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public Builder from(ServiceCall parent, List<ServiceCall> children, Instant now, boolean isBulk) {
            if (isBulk) {
                bulkConfirmationMessage = BULK_MESSAGE_FACTORY.createMessage(parent, children, now);
            } else {
                confirmationMessage = SINGLE_MESSAGE_FACTORY.createMessage(parent, children.get(0), now);
            }
            return this;
        }

        public Builder from(UtilitiesDeviceCreateRequestMessage message, MessageSeeds messageSeed, Instant now) {
            if (message.isBulk()) {
                bulkConfirmationMessage = BULK_MESSAGE_FACTORY.createMessage(message, messageSeed, now);
            } else {
                confirmationMessage = SINGLE_MESSAGE_FACTORY.createMessage(message, messageSeed, now);
            }
            return this;
        }

        public UtilitiesDeviceCreateConfirmationMessage build() {
            return UtilitiesDeviceCreateConfirmationMessage.this;
        }
    }
}
