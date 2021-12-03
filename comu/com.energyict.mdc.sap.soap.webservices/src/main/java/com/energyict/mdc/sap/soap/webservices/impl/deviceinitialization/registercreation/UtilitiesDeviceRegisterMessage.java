/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation;

import com.elster.jupiter.util.Checks;

import com.energyict.mdc.sap.soap.webservices.impl.AbstractSapMessage;

import java.time.Instant;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.MINUTES;

public class UtilitiesDeviceRegisterMessage extends AbstractSapMessage {

    private static final String LRN_XML_NAME = "UtilitiesMeasurementTaskID";

    private String obis;
    private String recurrenceCode;
    private String lrn;
    private String divisionCategory;
    private Instant startDate;
    private Instant endDate;
    private String timeZone;
    private String registerId;

    public String getObis() {
        return obis;
    }

    public String getRecurrenceCode() {
        return recurrenceCode;
    }

    public String getLrn() {
        return lrn;
    }

    public String getDivisionCategory() {
        return divisionCategory;
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

    public String getTimeZone() {
        return timeZone;
    }

    public String getRegisterId() {
        return registerId;
    }

    public class Builder {

        private Builder() {
        }

        public UtilitiesDeviceRegisterMessage.Builder from(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UtilsDvceERPSmrtMtrRegCrteReqReg requestMessage,
                                                           Integer lrnEndInterval) {
            setObis(getObis(requestMessage));
            setRecurrenceCode(getRecurrenceCode(requestMessage));
            setLrn(getLrn(requestMessage));
            setDivisionCategory(getDivisionCategory(requestMessage));
            setStartDate(requestMessage.getStartDate());
            setEndDate(requestMessage.getEndDate(), lrnEndInterval);
            setTimeZone(getTimeZone(requestMessage));
            setRegisterId(getRegisterId(requestMessage));

            return this;
        }

        public UtilitiesDeviceRegisterMessage.Builder from(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.UtilsDvceERPSmrtMtrRegCrteReqReg requestMessage,
                                                           Integer lrnEndInterval) {
            setObis(getObis(requestMessage));
            setRecurrenceCode(getRecurrenceCode(requestMessage));
            setLrn(getLrn(requestMessage));
            setDivisionCategory(getDivisionCategory(requestMessage));
            setStartDate(requestMessage.getStartDate());
            setEndDate(requestMessage.getEndDate(), lrnEndInterval);
            setTimeZone(getTimeZone(requestMessage));
            setRegisterId(getRegisterId(requestMessage));

            return this;
        }

        public UtilitiesDeviceRegisterMessage build() {
            if (lrn == null) {
                addMissingField(LRN_XML_NAME);
            }
            return UtilitiesDeviceRegisterMessage.this;
        }

        private void setLrn(String lrn) {
            UtilitiesDeviceRegisterMessage.this.lrn = lrn;
        }

        private void setObis(String obis) {
            UtilitiesDeviceRegisterMessage.this.obis = obis;
        }

        private void setRecurrenceCode(String interval) {
            UtilitiesDeviceRegisterMessage.this.recurrenceCode = interval;
        }

        private void setDivisionCategory(String divisionCategory) {
            UtilitiesDeviceRegisterMessage.this.divisionCategory = divisionCategory;
        }

        private void setStartDate(Instant startDate) {
            UtilitiesDeviceRegisterMessage.this.startDate = startDate;
        }

        private void setEndDate(Instant endDate, Integer lrnEndInterval) {
            UtilitiesDeviceRegisterMessage.this.endDate = endDate.plus(Optional.ofNullable(lrnEndInterval).orElse(0), MINUTES);
        }

        private void setTimeZone(String timeZone) {
            UtilitiesDeviceRegisterMessage.this.timeZone = timeZone;
        }

        private void setRegisterId(String registerId) {
            UtilitiesDeviceRegisterMessage.this.registerId = registerId;
        }

        private String getObis(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UtilsDvceERPSmrtMtrRegCrteReqReg requestMessage) {
            return Optional.ofNullable(requestMessage.getUtilitiesObjectIdentificationSystemCodeText())
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getObis(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.UtilsDvceERPSmrtMtrRegCrteReqReg requestMessage) {
            return Optional.ofNullable(requestMessage.getUtilitiesObjectIdentificationSystemCodeText())
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getRecurrenceCode(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UtilsDvceERPSmrtMtrRegCrteReqReg requestMessage) {
            return Optional.ofNullable(requestMessage.getUtilitiesMeasurementRecurrenceCode())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UtilitiesMeasurementRecurrenceCode::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getRecurrenceCode(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.UtilsDvceERPSmrtMtrRegCrteReqReg requestMessage) {
            return Optional.ofNullable(requestMessage.getUtilitiesMeasurementRecurrenceCode())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.UtilitiesMeasurementRecurrenceCode::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getDivisionCategory(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UtilsDvceERPSmrtMtrRegCrteReqReg requestMessage) {
            return Optional.ofNullable(requestMessage.getUtilitiesDivisionCategoryCode())
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getDivisionCategory(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.UtilsDvceERPSmrtMtrRegCrteReqReg requestMessage) {
            return Optional.ofNullable(requestMessage.getUtilitiesDivisionCategoryCode())
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

        private String getTimeZone(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UtilsDvceERPSmrtMtrRegCrteReqReg requestMessage) {
            return Optional.ofNullable(requestMessage.getTimeZoneCode())
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getTimeZone(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.UtilsDvceERPSmrtMtrRegCrteReqReg requestMessage) {
            return Optional.ofNullable(requestMessage.getTimeZoneCode())
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getRegisterId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UtilsDvceERPSmrtMtrRegCrteReqReg requestMessage) {
//            return Optional.ofNullable(requestMessage.getUtilitiesMeasurementTaskID())
//                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreaterequest.UtilitiesMeasurementTaskID::getValue)
//                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
//                    .orElse(null);
            return "01";
        }

        private String getRegisterId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.UtilsDvceERPSmrtMtrRegCrteReqReg requestMessage) {
//            return Optional.ofNullable(requestMessage.getUtilitiesMeasurementTaskID())
//                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreaterequest.UtilitiesMeasurementTaskID::getValue)
//                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
//                    .orElse(null);
            return "01";
        }
    }
}