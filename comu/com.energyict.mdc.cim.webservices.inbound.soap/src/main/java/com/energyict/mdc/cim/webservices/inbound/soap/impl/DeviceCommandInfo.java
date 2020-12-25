/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.energyict.mdc.device.data.impl.ami.EndDeviceControlTypeMapping;
import com.elster.jupiter.metering.ami.ChangeTaxRatesInfo;
import com.elster.jupiter.metering.ami.StepTariffInfo;

import java.math.BigDecimal;
import java.time.Instant;

public class DeviceCommandInfo {
    private EndDeviceControlTypeMapping endDeviceControlTypeMapping;
    private Instant activationDate;
    private BigDecimal creditDaysLimitFirst;
    private BigDecimal creditDaysLimitScnd;
    private String creditType;
    private BigDecimal creditAmount;
    private StepTariffInfo stepTariffInfo;
    private ChangeTaxRatesInfo changeTaxRatesInfo;
    private String tariffType;
    private String chargeMode;

    public EndDeviceControlTypeMapping getEndDeviceControlTypeMapping() {
        return endDeviceControlTypeMapping;
    }

    public void setEndDeviceControlTypeMapping(EndDeviceControlTypeMapping endDeviceControlTypeMapping) {
        this.endDeviceControlTypeMapping = endDeviceControlTypeMapping;
    }

    public Instant getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(Instant activationDate) {
        this.activationDate = activationDate;
    }

    public BigDecimal getCreditDaysLimitFirst() {
        return creditDaysLimitFirst;
    }

    public void setCreditDaysLimitFirst(BigDecimal creditDaysLimitFirst) {
        this.creditDaysLimitFirst = creditDaysLimitFirst;
    }

    public BigDecimal getCreditDaysLimitScnd() {
        return creditDaysLimitScnd;
    }

    public void setCreditDaysLimitScnd(BigDecimal creditDaysLimitScnd) {
        this.creditDaysLimitScnd = creditDaysLimitScnd;
    }

    public String getCreditType() {
        return creditType;
    }

    public void setCreditType(String creditType) {
        this.creditType = creditType;
    }

    public BigDecimal getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(BigDecimal creditAmount) {
        this.creditAmount = creditAmount;
    }

    public StepTariffInfo getStepTariffInfo() {
        return stepTariffInfo;
    }

    public void setStepTariffInfo(StepTariffInfo stepTariffInfo) {
        this.stepTariffInfo = stepTariffInfo;
    }


    public ChangeTaxRatesInfo getChangeTaxRatesInfo() {
        return changeTaxRatesInfo;
    }

    public void setChangeTaxRatesInfo(ChangeTaxRatesInfo changeTaxRatesInfo) {
        this.changeTaxRatesInfo = changeTaxRatesInfo;
    }

    public String getTariffType() {
        return tariffType;
    }

    public void setTariffType(String tariffType) {
        this.tariffType = tariffType;
    }

    public String getChargeMode() {
        return chargeMode;
    }

    public void setChargeMode(String chargeMode) {
        this.chargeMode = chargeMode;
    }
}