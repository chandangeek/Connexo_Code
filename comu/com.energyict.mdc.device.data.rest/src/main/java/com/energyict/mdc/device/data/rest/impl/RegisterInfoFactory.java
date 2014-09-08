package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.ValidationEvaluator;
import com.energyict.mdc.device.data.*;

import java.util.ArrayList;
import java.util.List;

public class RegisterInfoFactory {
    public static RegisterInfo asInfo(Register register, ValidationEvaluator evaluator) {
        if (register instanceof BillingRegister) {
            return new BillingRegisterInfo((BillingRegister)register, evaluator);
        } else if (register instanceof NumericalRegister) {
            return new NumericalRegisterInfo((NumericalRegister)register, evaluator);
        } else if (register instanceof TextRegister) {
            return new TextRegisterInfo((TextRegister)register);
        } else if (register instanceof FlagsRegister) {
            return new FlagsRegisterInfo((FlagsRegister)register);
        }

        throw new IllegalArgumentException("Unsupported register type: " + register.getClass().getSimpleName());
    }

    public static List<RegisterInfo> asInfoList(List<Register> registers, ValidationEvaluator evaluator) {
        List<RegisterInfo> registerInfos = new ArrayList<>(registers.size());
        for(Register register : registers) {
            registerInfos.add(RegisterInfoFactory.asInfo(register, evaluator));
        }

        return registerInfos;
    }
}
