package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.EventRegister;
import com.energyict.mdc.device.data.NumericalRegister;
import com.energyict.mdc.device.data.Register;

import java.util.ArrayList;
import java.util.List;

public class RegisterInfoFactory {
    public static RegisterInfo asInfo(Register register) {
        if(EventRegister.class.isAssignableFrom(register.getClass())) {
            return new EventRegisterInfo((EventRegister)register);
        } else if(NumericalRegister.class.isAssignableFrom(register.getClass())) {
            return new NumericalRegisterInfo((NumericalRegister)register);
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
