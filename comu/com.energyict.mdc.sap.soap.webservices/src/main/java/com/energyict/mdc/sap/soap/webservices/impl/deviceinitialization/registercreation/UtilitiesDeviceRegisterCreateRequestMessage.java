/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.UtilsDvceERPSmrtMtrRegBulkCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UtilsDvceERPSmrtMtrRegCrteReqMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UtilitiesDeviceRegisterCreateRequestMessage {

    private String requestID;
    private String uuid;
    private boolean bulk;
    private List<UtilitiesDeviceRegisterCreateMessage> utilitiesDeviceRegisterCreateMessages = new ArrayList<>();

    private UtilitiesDeviceRegisterCreateRequestMessage() {
    }

    public boolean isBulk() {
        return bulk;
    }

    public String getRequestID() {
        return requestID;
    }

    public String getUuid() {
        return uuid;
    }

    public List<UtilitiesDeviceRegisterCreateMessage> getUtilitiesDeviceRegisterCreateMessages() {
        return utilitiesDeviceRegisterCreateMessages;
    }

    static UtilitiesDeviceRegisterCreateRequestMessage.Builder builder() {
        return new UtilitiesDeviceRegisterCreateRequestMessage().new Builder();
    }

    public boolean isValid() {
        return requestID != null || uuid != null;
    }

    public class Builder {

        private Builder() {
        }

        public UtilitiesDeviceRegisterCreateRequestMessage.Builder from(UtilsDvceERPSmrtMtrRegCrteReqMsg requestMessage, Integer lrnEndInterval) {
            bulk = false;
            Optional.ofNullable(requestMessage.getMessageHeader())
                    .ifPresent(messageHeader -> {
                        setRequestID(getRequestID(messageHeader));
                        setUuid(getUuid(messageHeader));
                    });

            utilitiesDeviceRegisterCreateMessages.add(UtilitiesDeviceRegisterCreateMessage
                    .builder()
                    .from(requestMessage, lrnEndInterval)
                    .build());
            return this;
        }

        public UtilitiesDeviceRegisterCreateRequestMessage.Builder from(UtilsDvceERPSmrtMtrRegBulkCrteReqMsg requestMessage, Integer lrnEndInterval) {
            bulk = true;
            Optional.ofNullable(requestMessage.getMessageHeader())
                    .ifPresent(messageHeader -> {
                        setRequestID(getRequestID(messageHeader));
                        setUuid(getUuid(messageHeader));
                    });

            requestMessage.getUtilitiesDeviceERPSmartMeterRegisterCreateRequestMessage()
                    .forEach(message ->
                            utilitiesDeviceRegisterCreateMessages.add(UtilitiesDeviceRegisterCreateMessage
                                    .builder()
                                    .from(message, lrnEndInterval)
                                    .build()));
            return this;
        }

        public UtilitiesDeviceRegisterCreateRequestMessage build() {
            return UtilitiesDeviceRegisterCreateRequestMessage.this;
        }

        private void setRequestID(String requestID) {
            UtilitiesDeviceRegisterCreateRequestMessage.this.requestID = requestID;
        }

        private void setUuid(String uuid) {
            UtilitiesDeviceRegisterCreateRequestMessage.this.uuid = uuid;
        }

        private String getRequestID(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getRequestID(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getUuid(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getUUID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UUID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getUuid(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getUUID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.UUID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }
    }
}
