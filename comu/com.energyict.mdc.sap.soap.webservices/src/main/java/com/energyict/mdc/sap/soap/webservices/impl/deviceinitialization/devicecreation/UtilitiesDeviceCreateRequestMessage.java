/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UUID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrBlkCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UtilitiesDeviceCreateRequestMessage {

    private String requestID;
    private String uuid;
    private List<UtilitiesDeviceCreateMessage> utilitiesDeviceCreateMessages = new ArrayList<>();
    private boolean bulk;

    private UtilitiesDeviceCreateRequestMessage() {
    }

    public String getRequestID() {
        return requestID;
    }

    public String getUuid() {
        return uuid;
    }

    public List<UtilitiesDeviceCreateMessage> getUtilitiesDeviceCreateMessages() {
        return utilitiesDeviceCreateMessages;
    }

    public boolean isBulk() {
        return bulk;
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

        public UtilitiesDeviceCreateRequestMessage.Builder from(UtilsDvceERPSmrtMtrCrteReqMsg requestMessage) {
            bulk = false;
            Optional.ofNullable(requestMessage.getMessageHeader())
                    .ifPresent(messageHeader -> {
                        setRequestID(getRequestID(messageHeader));
                    });

            utilitiesDeviceCreateMessages.add(UtilitiesDeviceCreateMessage
                    .builder()
                    .from(requestMessage.getUtilitiesDevice())
                    .build());
            return this;
        }

        public UtilitiesDeviceCreateRequestMessage.Builder from(UtilsDvceERPSmrtMtrBlkCrteReqMsg requestMessage) {
            bulk = true;
            Optional.ofNullable(requestMessage.getMessageHeader())
                    .ifPresent(messageHeader -> {
                        setRequestID(getRequestID(messageHeader));
                        setUuid(getUuid(messageHeader));
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

        private void setUuid(String uuid) {
            UtilitiesDeviceCreateRequestMessage.this.uuid = uuid;
        }

        private String getRequestID(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getRequestID(com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getUuid(com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getUUID())
                    .map(UUID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }
    }
}
