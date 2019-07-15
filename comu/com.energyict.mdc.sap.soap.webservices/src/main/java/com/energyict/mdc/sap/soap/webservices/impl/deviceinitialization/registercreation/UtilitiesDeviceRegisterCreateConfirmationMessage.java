/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation;

import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceRegisterCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.UtilsDvceERPSmrtMtrRegBulkCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.UtilsDvceERPSmrtMtrRegCrteConfMsg;

import java.time.Instant;
import java.util.List;

public class UtilitiesDeviceRegisterCreateConfirmationMessage {

    private static final CreateBulkRegisterConfirmationMessageFactory BULK_MESSAGE_FACTORY = new CreateBulkRegisterConfirmationMessageFactory();
    private static final CreateRegisterConfirmationMessageFactory SINGLE_MESSAGE_FACTORY = new CreateRegisterConfirmationMessageFactory();

    private UtilsDvceERPSmrtMtrRegBulkCrteConfMsg bulkConfirmationMessage;
    private UtilsDvceERPSmrtMtrRegCrteConfMsg confirmationMessage;

    public UtilsDvceERPSmrtMtrRegBulkCrteConfMsg getBulkConfirmationMessage() {
        return bulkConfirmationMessage;
    }

    public UtilsDvceERPSmrtMtrRegCrteConfMsg getConfirmationMessage() {
        return confirmationMessage;
    }


    public static Builder builder() {
        return new UtilitiesDeviceRegisterCreateConfirmationMessage().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public Builder from(ServiceCall serviceCall, List<ServiceCall> children, Instant now, boolean isBulk) {
            if (isBulk) {
                bulkConfirmationMessage = BULK_MESSAGE_FACTORY.createMessage(serviceCall, children, now);
            } else {
                confirmationMessage = SINGLE_MESSAGE_FACTORY.createMessage(serviceCall, children, now);
            }
            return this;
        }

        public Builder from(UtilitiesDeviceRegisterCreateRequestMessage requestMessage, MessageSeeds messageSeed, Instant now) {
            if (requestMessage.isBulk()) {
                bulkConfirmationMessage = BULK_MESSAGE_FACTORY.createMessage(requestMessage, messageSeed, now);
            } else {
                confirmationMessage = SINGLE_MESSAGE_FACTORY.createMessage(requestMessage, messageSeed, now);
            }
            return this;
        }

        public UtilitiesDeviceRegisterCreateConfirmationMessage build() {
            return UtilitiesDeviceRegisterCreateConfirmationMessage.this;
        }
    }
}