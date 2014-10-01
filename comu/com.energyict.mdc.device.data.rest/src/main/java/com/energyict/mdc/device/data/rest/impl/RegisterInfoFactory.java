package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.BillingRegister;
import com.energyict.mdc.device.data.FlagsRegister;
import com.energyict.mdc.device.data.NumericalRegister;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.TextRegister;

import java.util.ArrayList;
import java.util.List;

public class RegisterInfoFactory {
    public static RegisterInfo asInfo(Register register, DetailedValidationInfo registerValidationInfo) {
        if (register instanceof BillingRegister) {
            return new BillingRegisterInfo((BillingRegister)register, registerValidationInfo);
        } else if (register instanceof NumericalRegister) {
            return new NumericalRegisterInfo((NumericalRegister)register, registerValidationInfo);
        } else if (register instanceof TextRegister) {
            return new TextRegisterInfo((TextRegister)register);
        } else if (register instanceof FlagsRegister) {
            return new FlagsRegisterInfo((FlagsRegister)register);
        }

        throw new IllegalArgumentException("Unsupported register type: " + register.getClass().getSimpleName());
    }

    public static List<RegisterInfo> asInfoList(List<Register> registers, ValidationInfoHelper validationInfoHelper) {
        List<RegisterInfo> registerInfos = new ArrayList<>(registers.size());
        for(Register register : registers) {
            registerInfos.add(RegisterInfoFactory.asInfo(register, validationInfoHelper.getRegisterValidationInfo(register)));
        }

        return registerInfos;
    }
}
