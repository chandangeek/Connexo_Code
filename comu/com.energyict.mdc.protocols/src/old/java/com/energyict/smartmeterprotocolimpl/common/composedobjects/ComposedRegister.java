/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.common.composedobjects;

import com.energyict.dlms.DLMSAttribute;

/**
 * The ComposedRegister is just a ValueObject that holds the {@link com.energyict.dlms.DLMSAttribute} from the register his value and unit
 */
public class ComposedRegister {

    private final DLMSAttribute registerValue;
    private final DLMSAttribute registerUnit;
    private final DLMSAttribute registerCaptureTime;

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

    public DLMSAttribute getRegisterValueAttribute() {
        return registerValue;
    }

    public DLMSAttribute getRegisterUnitAttribute() {
        return registerUnit;
    }

    public DLMSAttribute getRegisterCaptureTime() {
        return registerCaptureTime;
    }
}
