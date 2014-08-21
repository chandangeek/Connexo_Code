package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.NumericalRegister;
import org.codehaus.jackson.annotate.JsonProperty;

import java.math.BigDecimal;

public class NumericalRegisterInfo extends RegisterInfo<NumericalRegister, NumericalReading> {
    @JsonProperty("numberOfDigits")
    public Integer numberOfDigits;
    @JsonProperty("numberOfFractionDigits")
    public Integer numberOfFractionDigits;
    @JsonProperty("multiplier")
    public BigDecimal multiplier;
    @JsonProperty("overflow")
    public BigDecimal overflow;

    public NumericalRegisterInfo() {}

    public NumericalRegisterInfo(NumericalRegister register) {
        super(register);
        NumericalRegisterSpec registerSpec = (NumericalRegisterSpec)register.getRegisterSpec();
        this.numberOfDigits = registerSpec.getNumberOfDigits();
        this.numberOfFractionDigits = registerSpec.getNumberOfFractionDigits();
        this.multiplier = registerSpec.getMultiplier();
        this.overflow = registerSpec.getOverflowValue();
        this.multiplierMode = registerSpec.getMultiplierMode();
    }
}
