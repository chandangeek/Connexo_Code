package com.energyict.smartmeterprotocolimpl.webrtuz3.composedobjects;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.ComposedCosemObject;

/**
 * The ComposedRegister is just a ValueObject that holds the {@link com.energyict.dlms.DLMSAttribute} from the register his value and unit
 */
public class ComposedRegister {

    private final DLMSAttribute registerValue;
    private final DLMSAttribute registerUnit;

    public ComposedRegister(DLMSAttribute registerValue, DLMSAttribute registerUnit) {
        this.registerValue = registerValue;
        this.registerUnit = registerUnit;
    }

    public DLMSAttribute getRegisterValueAttribute() {
        return registerValue;
    }

    public DLMSAttribute getRegisterUnitAttribute() {
        return registerUnit;
    }
}
