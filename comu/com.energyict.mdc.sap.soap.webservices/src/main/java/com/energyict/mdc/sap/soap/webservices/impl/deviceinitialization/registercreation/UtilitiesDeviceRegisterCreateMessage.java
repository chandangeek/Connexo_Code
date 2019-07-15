/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation;

import com.elster.jupiter.util.Checks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UtilitiesDeviceRegisterCreateMessage {

    private String deviceId;
    private List<UtilitiesDeviceRegisterMessage> utilitiesDeviceRegisterMessage = new ArrayList<>();

    public List<UtilitiesDeviceRegisterMessage> getUtilitiesDeviceRegisterMessage() {
        return utilitiesDeviceRegisterMessage;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public boolean isValid() {
        return deviceId != null;
    }

    static UtilitiesDeviceRegisterCreateMessage.Builder builder() {
        return new UtilitiesDeviceRegisterCreateMessage().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public UtilitiesDeviceRegisterCreateMessage.Builder from(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UtilsDvceERPSmrtMtrRegCrteReqUtilsDvce requestMessage) {
            setDeviceId(getDeviceId(requestMessage));

            requestMessage.getRegister()
                    .forEach(message ->
                            utilitiesDeviceRegisterMessage.add(UtilitiesDeviceRegisterMessage
                                    .builder()
                                    .from(message)
                                    .build()));
            return this;
        }

        public UtilitiesDeviceRegisterCreateMessage.Builder from(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.UtilsDvceERPSmrtMtrRegCrteReqUtilsDvce requestMessage) {
            setDeviceId(getDeviceId(requestMessage));

            requestMessage.getRegister()
                    .forEach(message ->
                            utilitiesDeviceRegisterMessage.add(UtilitiesDeviceRegisterMessage
                                    .builder()
                                    .from(message)
                                    .build()));
            return this;
        }

        public UtilitiesDeviceRegisterCreateMessage build() {
            return UtilitiesDeviceRegisterCreateMessage.this;
        }

        private void setDeviceId(String deviceId) {
            UtilitiesDeviceRegisterCreateMessage.this.deviceId = deviceId;
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
