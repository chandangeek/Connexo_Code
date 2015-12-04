package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class NumericalRegisterInfo extends RegisterInfo {

    @JsonProperty("numberOfFractionDigits")
    public Integer numberOfFractionDigits;
    @JsonProperty("overflow")
    public BigDecimal overflow;
    public DetailedValidationInfo detailedValidationInfo;
    public BigDecimal multiplier;
    @JsonProperty("calculatedReadingType")
    public ReadingTypeInfo calculatedReadingType;

    public NumericalRegisterInfo() {}

}
