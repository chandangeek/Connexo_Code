/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce;

import java.util.Optional;

public class UtilitiesDeviceCreateMessage {
    private String serialId;
    private String deviceId;

    static UtilitiesDeviceCreateMessage.Builder builder() {
        return new UtilitiesDeviceCreateMessage().new Builder();
    }

    public boolean isValid() {
        return serialId != null && deviceId != null;
    }

    public String getSerialId() {
        return serialId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public class Builder {

        private Builder() {
        }

        public UtilitiesDeviceCreateMessage.Builder from(UtilsDvceERPSmrtMtrCrteReqUtilsDvce requestMessage) {
            setSerialId(getSerialId(requestMessage));
            setDeviceId(getDeviceId(requestMessage));
            return this;
        }

        public UtilitiesDeviceCreateMessage build() {
            return UtilitiesDeviceCreateMessage.this;
        }

        private void setSerialId(String serialId) {
            UtilitiesDeviceCreateMessage.this.serialId = serialId;
        }

        private void setDeviceId(String deviceId) {
            UtilitiesDeviceCreateMessage.this.deviceId = deviceId;
        }

        private String getDeviceId(UtilsDvceERPSmrtMtrCrteReqUtilsDvce requestMessage) {
            return Optional.ofNullable(requestMessage.getID())
                    .map(UtilitiesDeviceID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getSerialId(UtilsDvceERPSmrtMtrCrteReqUtilsDvce requestMessage) {
            return Optional.ofNullable(requestMessage.getSerialID())
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }
    }

}
