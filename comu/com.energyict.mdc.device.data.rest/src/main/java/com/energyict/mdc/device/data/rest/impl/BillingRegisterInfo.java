package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.ValidationEvaluator;
import com.energyict.mdc.device.data.BillingReading;
import com.energyict.mdc.device.data.BillingRegister;

public class BillingRegisterInfo extends RegisterInfo<BillingRegister, BillingReading> {

    public BillingRegisterInfo() {
    }

    public BillingRegisterInfo(BillingRegister register, ValidationEvaluator evaluator) {
        super(register, evaluator);
    }

}