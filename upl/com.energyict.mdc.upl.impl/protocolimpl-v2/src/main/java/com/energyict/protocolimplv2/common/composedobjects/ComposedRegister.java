package com.energyict.protocolimplv2.common.composedobjects;

import com.energyict.dlms.DLMSAttribute;

/**
 * The ComposedRegister is just a ValueObject that holds the {@link com.energyict.dlms.DLMSAttribute} from the register his value and unit
 */
public class ComposedRegister implements ComposedObject {

    private DLMSAttribute registerValue;
    private DLMSAttribute registerUnit;
    private DLMSAttribute registerCaptureTime;

    public ComposedRegister(DLMSAttribute registerValue, DLMSAttribute registerUnit, DLMSAttribute registerCaptureTime) {
        this.registerValue = registerValue;
        this.registerUnit = registerUnit;
        this.registerCaptureTime = registerCaptureTime;
    }

    public ComposedRegister(DLMSAttribute registerValue, DLMSAttribute registerUnit) {
        this.registerValue = registerValue;
        this.registerUnit = registerUnit;
        this.registerCaptureTime = null;
    }

    public ComposedRegister() {
    }

    public DLMSAttribute getRegisterValueAttribute() {
        return registerValue;
    }

    public DLMSAttribute getRegisterUnitAttribute() {
        return registerUnit;
    }

    public DLMSAttribute getRegisterCaptureTime() {
        return registerCaptureTime;
    }

    public void setRegisterCaptureTime(DLMSAttribute registerCaptureTime) {
        this.registerCaptureTime = registerCaptureTime;
    }

    public void setRegisterValue(DLMSAttribute registerValue) {
        this.registerValue = registerValue;
    }

    public void setRegisterUnit(DLMSAttribute registerUnit) {
        this.registerUnit = registerUnit;
    }
}