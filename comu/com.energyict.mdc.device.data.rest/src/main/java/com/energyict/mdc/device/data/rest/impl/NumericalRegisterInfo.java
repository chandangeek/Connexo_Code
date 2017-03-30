/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class NumericalRegisterInfo extends RegisterInfo {

    @JsonProperty("numberOfFractionDigits")
    public Integer numberOfFractionDigits;
    @JsonProperty("overruledNumberOfFractionDigits")
    public Integer overruledNumberOfFractionDigits;
    @JsonProperty("overflow")
    public BigDecimal overflow;
    @JsonProperty("overruledOverflow")
    public BigDecimal overruledOverflow;
    public DetailedValidationInfo detailedValidationInfo;
    public BigDecimal multiplier;
    public Boolean useMultiplier;
    @JsonProperty("calculatedReadingType")
    public ReadingTypeInfo calculatedReadingType;

    public NumericalRegisterInfo() {}

}
