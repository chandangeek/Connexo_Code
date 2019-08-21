/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrBlkCrteReqMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UtilitiesDeviceCreateRequestMessage {

    private String requestID;
    private List<UtilitiesDeviceCreateMessage> utilitiesDeviceCreateMessages = new ArrayList<>();

    private UtilitiesDeviceCreateRequestMessage() {
    }

    public String getRequestID() {
        return requestID;
    }

    public List<UtilitiesDeviceCreateMessage> getUtilitiesDeviceCreateMessages() {
        return utilitiesDeviceCreateMessages;
    }

    static UtilitiesDeviceCreateRequestMessage.Builder builder() {
        return new UtilitiesDeviceCreateRequestMessage().new Builder();
    }

    public boolean isValid() {
        return requestID != null;
    }

    public class Builder {

        private Builder() {
        }

        public UtilitiesDeviceCreateRequestMessage.Builder from(UtilsDvceERPSmrtMtrBlkCrteReqMsg requestMessage) {
            Optional.ofNullable(requestMessage.getMessageHeader())
                    .ifPresent(messageHeader -> {
                        setRequestID(getRequestID(messageHeader));
                    });

            requestMessage.getUtilitiesDeviceERPSmartMeterCreateRequestMessage()
                    .forEach(message ->
                            utilitiesDeviceCreateMessages.add(UtilitiesDeviceCreateMessage
                                    .builder()
                                    .from(message.getUtilitiesDevice())
                                    .build()));
            return this;
        }

        public UtilitiesDeviceCreateRequestMessage build() {
            return UtilitiesDeviceCreateRequestMessage.this;
        }

        private void setRequestID(String requestID) {
            UtilitiesDeviceCreateRequestMessage.this.requestID = requestID;
        }

        private String getRequestID(BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getID())
                    .map(BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }
    }
}
