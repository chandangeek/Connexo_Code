/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Checks;

import com.energyict.mdc.sap.soap.webservices.impl.AbstractSapMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UtilitiesDeviceRegisterCreateMessage extends AbstractSapMessage {

    private static final String DEVICE_ID_XML_NAME = "UtilitiesDevice.ID";

    private String requestId;
    private String uuid;
    private String deviceId;
    private List<UtilitiesDeviceRegisterMessage> utilitiesDeviceRegisterMessages = new ArrayList<>();

    public List<UtilitiesDeviceRegisterMessage> getUtilitiesDeviceRegisterMessages() {
        return utilitiesDeviceRegisterMessages;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getUuid() {
        return uuid;
    }

    public String getDeviceId() {
        return deviceId;
    }

    static UtilitiesDeviceRegisterCreateMessage.Builder builder() {
        return new UtilitiesDeviceRegisterCreateMessage().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public UtilitiesDeviceRegisterCreateMessage.Builder from(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UtilsDvceERPSmrtMtrRegCrteReqMsg requestMessage,
                                                                 Integer lrnEndInterval) {
            setRequestId(getRequestId(requestMessage.getMessageHeader()));
            setUuid(getUuid(requestMessage.getMessageHeader()));
            Optional.ofNullable(requestMessage.getUtilitiesDevice())
                    .ifPresent(request -> {
                        setDeviceId(getDeviceId(request));

                        request.getRegister()
                                .forEach(message ->
                                        utilitiesDeviceRegisterMessages.add(UtilitiesDeviceRegisterMessage
                                                .builder()
                                                .from(message, lrnEndInterval)
                                                .build()));
                    });
            return this;
        }

        public UtilitiesDeviceRegisterCreateMessage.Builder from(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.UtilsDvceERPSmrtMtrRegCrteReqMsg requestMessage,
                                                                 Integer lrnEndInterval) {
            setRequestId(getRequestId(requestMessage.getMessageHeader()));
            setUuid(getUuid(requestMessage.getMessageHeader()));
            Optional.ofNullable(requestMessage.getUtilitiesDevice())
                    .ifPresent(request -> {
                        setDeviceId(getDeviceId(request));

                        request.getRegister()
                                .forEach(message ->
                                        utilitiesDeviceRegisterMessages.add(UtilitiesDeviceRegisterMessage
                                                .builder()
                                                .from(message, lrnEndInterval)
                                                .build()));
                    });
            return this;
        }

        public UtilitiesDeviceRegisterCreateMessage build(Thesaurus thesaurus) {
            if (requestId == null && uuid == null) {
                addAtLeastOneMissingField(thesaurus, REQUEST_ID_XML_NAME, UUID_XML_NAME);
            }
            if (deviceId == null) {
                addMissingField(DEVICE_ID_XML_NAME);
            }
            utilitiesDeviceRegisterMessages.forEach(utilitiesDeviceRegisterMessage -> addMissingFields(utilitiesDeviceRegisterMessage.getMissingFieldsSet()));
            return UtilitiesDeviceRegisterCreateMessage.this;
        }

        private void setRequestId(String requestId) {
            UtilitiesDeviceRegisterCreateMessage.this.requestId = requestId;
        }

        private void setUuid(String uuid) {
            UtilitiesDeviceRegisterCreateMessage.this.uuid = uuid;
        }

        private void setDeviceId(String deviceId) {
            UtilitiesDeviceRegisterCreateMessage.this.deviceId = deviceId;
        }

        private String getRequestId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.BusinessDocumentMessageHeader request) {
            return Optional.ofNullable(request)
                    .map(m -> m.getID())
                    .map(id -> id.getValue())
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getRequestId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.BusinessDocumentMessageHeader request) {
            return Optional.ofNullable(request)
                    .map(m -> m.getID())
                    .map(id -> id.getValue())
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getUuid(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.BusinessDocumentMessageHeader request) {
            return Optional.ofNullable(request)
                    .map(m -> m.getUUID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UUID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getUuid(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.BusinessDocumentMessageHeader request) {
            return Optional.ofNullable(request)
                    .map(m -> m.getUUID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.UUID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getDeviceId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UtilsDvceERPSmrtMtrRegCrteReqUtilsDvce requestMessage) {
            return Optional.ofNullable(requestMessage.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UtilitiesDeviceID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getDeviceId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.UtilsDvceERPSmrtMtrRegCrteReqUtilsDvce requestMessage) {
            return Optional.ofNullable(requestMessage.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.UtilitiesDeviceID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }
    }
}
