/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreplacement;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.webservices.impl.AdditionalProperties;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacement.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacement.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacement.UtilsDvceERPSmrtMtrRegChgReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacement.UtilsDvceERPSmrtMtrRegChgReqReg;

import java.time.Instant;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.MINUTES;

public class MeterRegisterChangeMessage {
    private String id;
    private String deviceId;
    private String lrn;
    private Instant endDate;
    private String timeZone;

    static MeterRegisterChangeMessage.Builder builder() {
        return new MeterRegisterChangeMessage().new Builder();
    }

    public boolean isValid() {
        return id != null && deviceId != null &&
                lrn != null && endDate != null;
    }

    public String getId() {
        return id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getLrn() {
        return lrn;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public class Builder {

        private Builder() {
        }

        public MeterRegisterChangeMessage.Builder from(UtilsDvceERPSmrtMtrRegChgReqMsg requestMessage) {
            setId(getId(requestMessage));
            setDeviceId(getDeviceId(requestMessage));
            setLrn(getLrn(requestMessage));
            setEndDate(calculateEndDate(requestMessage));
            setTimeZone(getTimeZone(requestMessage));
            return this;
        }

        public MeterRegisterChangeMessage build() {
            return MeterRegisterChangeMessage.this;
        }

        private void setId(String id) {
            MeterRegisterChangeMessage.this.id = id;
        }

        private void setDeviceId(String deviceId) {
            MeterRegisterChangeMessage.this.deviceId = deviceId;
        }

        private void setLrn(String lrn) {
            MeterRegisterChangeMessage.this.lrn = lrn;
        }

        private void setEndDate(Instant endDate) {
            MeterRegisterChangeMessage.this.endDate = endDate;
        }

        private void setTimeZone(String timeZone) {
            MeterRegisterChangeMessage.this.timeZone = timeZone;
        }

        private String getDeviceId(UtilsDvceERPSmrtMtrRegChgReqMsg requestMessage) {
            return Optional.ofNullable(requestMessage.getUtilitiesDevice().getID())
                    .map(UtilitiesDeviceID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElseThrow(null);
        }

        private String getId(UtilsDvceERPSmrtMtrRegChgReqMsg requestMessage) {
            return Optional.ofNullable(requestMessage.getMessageHeader().getID())
                    .map(BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getLrn(UtilsDvceERPSmrtMtrRegChgReqMsg requestMessage) {
            Optional<UtilsDvceERPSmrtMtrRegChgReqReg> register = requestMessage.getUtilitiesDevice().getRegister().stream().findFirst();
            if (register.isPresent() && !Checks.is(register.get().getUtilitiesMeasurementTaskID().getValue()).emptyOrOnlyWhiteSpace()) {
                return register.get().getUtilitiesMeasurementTaskID().getValue();
            } else {
                return null;
            }
        }

        private String getTimeZone(UtilsDvceERPSmrtMtrRegChgReqMsg requestMessage) {
            Optional<UtilsDvceERPSmrtMtrRegChgReqReg> register = requestMessage.getUtilitiesDevice().getRegister().stream().findFirst();
            if (register.isPresent() && !Checks.is(register.get().getTimeZoneCode()).emptyOrOnlyWhiteSpace()) {
                return register.get().getTimeZoneCode();
            } else {
                return null;
            }
        }

        private Instant calculateEndDate(UtilsDvceERPSmrtMtrRegChgReqMsg requestMessage) {
            Optional<UtilsDvceERPSmrtMtrRegChgReqReg> register = requestMessage.getUtilitiesDevice().getRegister().stream().findFirst();
            if (register.isPresent()) {
                return register.get().getEndDate().plus(WebServiceActivator.SAP_PROPERTIES.get(AdditionalProperties.METER_REPLACEMENT_ADD_INTERVAL), MINUTES);
            } else {
                return null;
            }
        }

    }

}
