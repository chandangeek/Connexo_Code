/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractSapMessage;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreaterequest.UUID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrBlkCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UtilitiesDeviceCreateRequestMessage extends AbstractSapMessage {

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

    public class Builder {

        private Builder() {
        }

        public UtilitiesDeviceCreateRequestMessage.Builder from(UtilsDvceERPSmrtMtrCrteReqMsg requestMessage) {
            bulk = false;
            Optional.ofNullable(requestMessage.getMessageHeader())
                    .ifPresent(messageHeader -> {
                        setRequestID(getRequestID(messageHeader));
                        setUuid(getUuid(messageHeader));
                    });

            utilitiesDeviceCreateMessages.add(UtilitiesDeviceCreateMessage
                    .builder()
                    .from(requestMessage)
                    .build());
            return this;
        }

        public UtilitiesDeviceCreateRequestMessage.Builder from(UtilsDvceERPSmrtMtrBlkCrteReqMsg requestMessage, Thesaurus thesaurus) {
            bulk = true;
            Optional.ofNullable(requestMessage.getMessageHeader())
                    .ifPresent(messageHeader -> {
                        setRequestID(getRequestID(messageHeader));
                        setUuid(getUuid(messageHeader));
                    });

            requestMessage.getUtilitiesDeviceERPSmartMeterCreateRequestMessage()
                    .forEach(message -> utilitiesDeviceCreateMessages.add(UtilitiesDeviceCreateMessage
                            .builder()
                            .from(message)
                            .build())
                    );
            if (requestID == null && uuid == null) {
                addAtLeastOneMissingField(thesaurus, REQUEST_ID_XML_NAME, UUID_XML_NAME);
            }
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

        private String getRequestID(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreaterequest.BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreaterequest.BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getUuid(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreaterequest.BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getUUID())
                    .map(UUID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getUuid(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getUUID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UUID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }
    }
}
