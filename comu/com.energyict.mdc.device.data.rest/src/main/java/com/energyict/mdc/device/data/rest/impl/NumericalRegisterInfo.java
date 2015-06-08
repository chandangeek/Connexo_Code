package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.NumericalRegister;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class NumericalRegisterInfo extends RegisterInfo<NumericalRegister, NumericalReading> {
    @JsonProperty("numberOfDigits")
    public Integer numberOfDigits;
    @JsonProperty("numberOfFractionDigits")
    public Integer numberOfFractionDigits;
    @JsonProperty("overflow")
    public BigDecimal overflow;
    public DetailedValidationInfo detailedValidationInfo;

    public NumericalRegisterInfo() {}

}
