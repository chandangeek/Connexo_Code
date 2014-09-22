package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;
import com.energyict.mdc.device.data.BillingReading;
import com.energyict.mdc.device.data.BillingRegister;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Date;
import java.util.Map;
import java.util.Set;

public class BillingRegisterInfo extends RegisterInfo<BillingRegister, BillingReading> {
    public DetailedValidationInfo detailedValidationInfo;

    public BillingRegisterInfo() {
    }

    public BillingRegisterInfo(BillingRegister register, DetailedValidationInfo registerValidationInfo, ValidationEvaluator evaluator) {
        super(register, evaluator);
        this.detailedValidationInfo = registerValidationInfo;
    }
}