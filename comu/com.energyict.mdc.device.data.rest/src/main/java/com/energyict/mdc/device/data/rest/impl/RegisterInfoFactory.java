package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.*;

import java.util.ArrayList;
import java.util.List;

public class RegisterInfoFactory {
    public static RegisterInfo asInfo(Register register) {
        if (register instanceof BillingRegister) {
            return new BillingRegisterInfo((BillingRegister)register);
        } else if (register instanceof NumericalRegister) {
            return new NumericalRegisterInfo((NumericalRegister)register);
        } else if (register instanceof TextRegister) {
            return new TextRegisterInfo((TextRegister)register);
        } else if (register instanceof FlagsRegister) {
            return new FlagsRegisterInfo((FlagsRegister)register);
        }

        throw new IllegalArgumentException("Unsupported register type: " + register.getClass().getSimpleName());
    }

    public static List<RegisterInfo> asInfoList(List<Register> registers) {
        List<RegisterInfo> registerInfos = new ArrayList<>(registers.size());
        for(Register register : registers) {
            registerInfos.add(RegisterInfoFactory.asInfo(register));
        }

        return registerInfos;
    }
}
