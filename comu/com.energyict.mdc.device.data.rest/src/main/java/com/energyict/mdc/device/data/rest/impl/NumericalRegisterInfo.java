package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.NumericalRegister;
import org.codehaus.jackson.annotate.JsonProperty;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class NumericalRegisterInfo extends RegisterInfo<NumericalRegister, NumericalReading> {
    @JsonProperty("numberOfDigits")
    public Integer numberOfDigits;
    @JsonProperty("numberOfFractionDigits")
    public Integer numberOfFractionDigits;
    @JsonProperty("multiplier")
    public BigDecimal multiplier;
    @JsonProperty("overflow")
    public BigDecimal overflow;
    public DetailedValidationInfo detailedValidationInfo;

    public NumericalRegisterInfo() {}

    public NumericalRegisterInfo(NumericalRegister register, DetailedValidationInfo registerValidationInfo, ValidationEvaluator evaluator) {
        super(register, evaluator);
        NumericalRegisterSpec registerSpec = (NumericalRegisterSpec)register.getRegisterSpec();
        this.numberOfDigits = registerSpec.getNumberOfDigits();
        this.numberOfFractionDigits = registerSpec.getNumberOfFractionDigits();
        this.multiplier = registerSpec.getMultiplier();
        this.overflow = registerSpec.getOverflowValue();
        this.multiplierMode = registerSpec.getMultiplierMode();
        this.detailedValidationInfo = registerValidationInfo;
    }
}
