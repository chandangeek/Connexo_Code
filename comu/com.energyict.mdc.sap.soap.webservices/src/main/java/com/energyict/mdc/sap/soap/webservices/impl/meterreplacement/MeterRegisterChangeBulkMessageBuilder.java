/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.meterreplacement;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkrequest.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkrequest.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkrequest.UUID;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkrequest.UtilsDvceERPSmrtMtrRegChgReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkrequest.UtilsDvceERPSmrtMtrRegChgReqReg;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkrequest.UtilitiesMeasurementRecurrenceCode;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.MINUTES;

public class MeterRegisterChangeBulkMessageBuilder {
    private final Integer lrnEndInterval;

    private MeterRegisterChangeMessage message = new MeterRegisterChangeMessage();

    private MeterRegisterChangeBulkMessageBuilder(Integer lrnEndInterval) {
        this.lrnEndInterval = lrnEndInterval;
    }

    public static MeterRegisterChangeBulkMessageBuilder builder(Integer lrnEndInterval) {
        return new MeterRegisterChangeBulkMessageBuilder(lrnEndInterval);
    }

    public MeterRegisterChangeBulkMessageBuilder from(UtilsDvceERPSmrtMtrRegChgReqMsg requestMessage) {
        setId(getId(requestMessage));
        setUuid(getUuid(requestMessage));
        setDeviceId(getDeviceId(requestMessage));
        addRegisters(getRegisters(requestMessage));
        return this;
    }

    public MeterRegisterChangeMessage build() {
        return message;
    }

    private void setId(String id) {
        message.setId(id);
    }

    private void setUuid(String uuid) {
        message.setUuid(uuid);
    }

    private void setDeviceId(String deviceId) {
        message.setDeviceId(deviceId);
    }

    private void addRegisters(List<RegisterChangeMessage> registers) {
        message.getRegisters().addAll(registers);
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

    private String getUuid(UtilsDvceERPSmrtMtrRegChgReqMsg requestMessage) {
        return Optional.ofNullable(requestMessage.getMessageHeader().getUUID())
                .map(UUID::getValue)
                .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                .orElse(null);
    }

    private List<RegisterChangeMessage> getRegisters(UtilsDvceERPSmrtMtrRegChgReqMsg requestMessage) {
        return requestMessage.getUtilitiesDevice().getRegister().stream().map(reg -> getRegister(reg)).collect(Collectors.toList());
    }

    private RegisterChangeMessage getRegister(UtilsDvceERPSmrtMtrRegChgReqReg reg) {
        RegisterChangeMessage register = new RegisterChangeMessage();
        register.setLrn(getLrn(reg));
        register.setStartDate(reg.getStartDate());
        register.setEndDate(calculateEndDate(reg));
        register.setCreateEndDate(reg.getEndDate());
        register.setTimeZone(getTimeZone(reg));
        register.setObis(getObis(reg));
        register.setRecurrenceCode(getRecurrenceCode(reg));
        register.setDivisionCategory(getDivisionCategory(reg));
        return register;
    }

    private String getLrn(UtilsDvceERPSmrtMtrRegChgReqReg requestRegister) {
        if (!Checks.is(requestRegister.getUtilitiesMeasurementTaskID().getValue()).emptyOrOnlyWhiteSpace()) {
            return requestRegister.getUtilitiesMeasurementTaskID().getValue();
        } else {
            return null;
        }
    }

    private String getTimeZone(UtilsDvceERPSmrtMtrRegChgReqReg requestRegister) {
        if (!Checks.is(requestRegister.getTimeZoneCode()).emptyOrOnlyWhiteSpace()) {
            return requestRegister.getTimeZoneCode();
        } else {
            return null;
        }
    }

    private Instant calculateEndDate(UtilsDvceERPSmrtMtrRegChgReqReg requestRegister) {
        return requestRegister.getEndDate().plus(lrnEndInterval, MINUTES);
    }

    private String getObis(UtilsDvceERPSmrtMtrRegChgReqReg requestRegister) {
        return Optional.ofNullable(requestRegister.getUtilitiesObjectIdentificationSystemCodeText())
                .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                .orElse(null);
    }

    private String getRecurrenceCode(UtilsDvceERPSmrtMtrRegChgReqReg requestRegister) {
        return Optional.ofNullable(requestRegister.getUtilitiesMeasurementRecurrenceCode())
                .map(UtilitiesMeasurementRecurrenceCode::getValue)
                .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                .orElse(null);
    }

    private String getDivisionCategory(UtilsDvceERPSmrtMtrRegChgReqReg requestRegister) {
        return Optional.ofNullable(requestRegister.getUtilitiesDivisionCategoryCode())
                .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                .orElse(null);
    }
}
