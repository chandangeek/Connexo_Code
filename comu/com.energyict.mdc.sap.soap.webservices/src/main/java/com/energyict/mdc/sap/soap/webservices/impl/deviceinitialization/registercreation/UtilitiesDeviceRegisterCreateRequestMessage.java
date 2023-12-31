/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractSapMessage;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.UtilsDvceERPSmrtMtrRegBulkCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UtilsDvceERPSmrtMtrRegCrteReqMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UtilitiesDeviceRegisterCreateRequestMessage extends AbstractSapMessage {

    private String requestID;
    private String uuid;
    private boolean bulk;
    private Thesaurus thesaurus;
    private List<UtilitiesDeviceRegisterCreateMessage> utilitiesDeviceRegisterCreateMessages = new ArrayList<>();

    private UtilitiesDeviceRegisterCreateRequestMessage(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
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

    static UtilitiesDeviceRegisterCreateRequestMessage.Builder builder(Thesaurus thesaurus) {
        return new UtilitiesDeviceRegisterCreateRequestMessage(thesaurus).new Builder();
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
                    .build(thesaurus));
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
                                    .build(thesaurus)));
            return this;
        }

        public UtilitiesDeviceRegisterCreateRequestMessage build(Thesaurus thesaurus) {
            if (requestID == null && uuid == null) {
                addAtLeastOneMissingField(thesaurus, REQUEST_ID_XML_NAME, UUID_XML_NAME);
            }
            utilitiesDeviceRegisterCreateMessages.forEach(utilitiesDeviceRegisterCreateMessage -> addMissingFields(utilitiesDeviceRegisterCreateMessage.getMissingFieldsSet()));
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
