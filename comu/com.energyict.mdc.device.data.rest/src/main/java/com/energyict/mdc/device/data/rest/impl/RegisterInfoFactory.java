package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.energyict.mdc.device.data.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RegisterInfoFactory {
    public static RegisterInfo asInfo(Register register, DetailedValidationInfo registerValidationInfo, ValidationEvaluator evaluator) {
        if (register instanceof BillingRegister) {
            return new BillingRegisterInfo((BillingRegister)register, registerValidationInfo, evaluator);
        } else if (register instanceof NumericalRegister) {
            return new NumericalRegisterInfo((NumericalRegister)register, registerValidationInfo, evaluator);
        } else if (register instanceof TextRegister) {
            return new TextRegisterInfo((TextRegister)register);
        } else if (register instanceof FlagsRegister) {
            return new FlagsRegisterInfo((FlagsRegister)register);
        }

        throw new IllegalArgumentException("Unsupported register type: " + register.getClass().getSimpleName());
    }

    public static List<RegisterInfo> asInfoList(List<Register> registers, ValidationInfoHelper validationInfoHelper, ValidationEvaluator evaluator) {
        List<RegisterInfo> registerInfos = new ArrayList<>(registers.size());
        for(Register register : registers) {
            registerInfos.add(RegisterInfoFactory.asInfo(register, validationInfoHelper.getRegisterValidationInfo(register), evaluator));
        }

        return registerInfos;
    }
}
