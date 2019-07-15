/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation;

import com.elster.jupiter.util.Checks;

import java.time.Instant;
import java.util.Optional;

public class UtilitiesDeviceRegisterMessage {
    private String OBIS;
    private String lrn;
    private Instant startDate;
    private Instant endDate;

    public String getOBIS() {
        return OBIS;
    }

    public String getLrn() {
        return lrn;
    }

    static UtilitiesDeviceRegisterMessage.Builder builder() {
        return new UtilitiesDeviceRegisterMessage().new Builder();
    }

    public Instant getStartDate() {
        return startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public boolean isValid() {
        return lrn != null && OBIS != null;
    }

    public class Builder {

        private Builder() {
        }

        public UtilitiesDeviceRegisterMessage.Builder from(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UtilsDvceERPSmrtMtrRegCrteReqReg requestMessage) {
            setOBIS(getOBIS(requestMessage));
            setLrn(getLrn(requestMessage));
            setStartDate(requestMessage.getStartDate());
            setEndDate(requestMessage.getEndDate());

            return this;
        }

        public UtilitiesDeviceRegisterMessage.Builder from(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.UtilsDvceERPSmrtMtrRegCrteReqReg requestMessage) {
            setOBIS(getOBIS(requestMessage));
            setLrn(getLrn(requestMessage));
            setStartDate(requestMessage.getStartDate());
            setEndDate(requestMessage.getEndDate());

            return this;
        }

        public UtilitiesDeviceRegisterMessage build() {
            return UtilitiesDeviceRegisterMessage.this;
        }

        private void setLrn(String lrn) {
            UtilitiesDeviceRegisterMessage.this.lrn = lrn;
        }

        private void setOBIS(String OBIS) {
            UtilitiesDeviceRegisterMessage.this.OBIS = OBIS;
        }

        private void setStartDate(Instant startDate) {
            UtilitiesDeviceRegisterMessage.this.startDate = startDate;
        }

        private void setEndDate(Instant endDate) {
            UtilitiesDeviceRegisterMessage.this.endDate = endDate;
        }

        private String getOBIS(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UtilsDvceERPSmrtMtrRegCrteReqReg requestMessage) {
            return Optional.ofNullable(requestMessage.getUtilitiesObjectIdentificationSystemCodeText())
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getOBIS(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.UtilsDvceERPSmrtMtrRegCrteReqReg requestMessage) {
            return Optional.ofNullable(requestMessage.getUtilitiesObjectIdentificationSystemCodeText())
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getLrn(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UtilsDvceERPSmrtMtrRegCrteReqReg requestMessage) {
            return Optional.ofNullable(requestMessage.getUtilitiesMeasurementTaskID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UtilitiesMeasurementTaskID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getLrn(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.UtilsDvceERPSmrtMtrRegCrteReqReg requestMessage) {
            return Optional.ofNullable(requestMessage.getUtilitiesMeasurementTaskID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.UtilitiesMeasurementTaskID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }
    }

}
