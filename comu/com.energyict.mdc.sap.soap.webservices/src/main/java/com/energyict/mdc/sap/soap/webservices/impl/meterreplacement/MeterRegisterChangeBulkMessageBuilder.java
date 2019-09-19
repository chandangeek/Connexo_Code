/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.meterreplacement;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.webservices.impl.AdditionalProperties;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkrequest.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkrequest.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkrequest.UtilsDvceERPSmrtMtrRegChgReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkrequest.UtilsDvceERPSmrtMtrRegChgReqReg;

import java.time.Instant;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.MINUTES;

public class MeterRegisterChangeBulkMessageBuilder {

    private MeterRegisterChangeMessage message = new MeterRegisterChangeMessage();

    private MeterRegisterChangeBulkMessageBuilder() {
    }

    public static MeterRegisterChangeBulkMessageBuilder builder() {
        return new MeterRegisterChangeBulkMessageBuilder();
    }

    public MeterRegisterChangeBulkMessageBuilder from(UtilsDvceERPSmrtMtrRegChgReqMsg requestMessage) {
        setId(getId(requestMessage));
        setDeviceId(getDeviceId(requestMessage));
        setLrn(getLrn(requestMessage));
        setEndDate(calculateEndDate(requestMessage));
        setTimeZone(getTimeZone(requestMessage));
        return this;
    }

    public MeterRegisterChangeMessage build() {
        return message;
    }

    private void setId(String id) {
        message.setId(id);
    }

    private void setDeviceId(String deviceId) {
        message.setDeviceId(deviceId);
    }

    private void setLrn(String lrn) {
        message.setLrn(lrn);
    }

    private void setEndDate(Instant endDate) {
        message.setEndDate(endDate);
    }

    private void setTimeZone(String timeZone) {
        message.setTimeZone(timeZone);
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
