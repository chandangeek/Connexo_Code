package com.energyict.genericprotocolimpl.common.pooling;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;

/**
 * Straightforward implementation of the <CODE>RtuRegisterFullProtocolShadow</CODE> interface
 */
public class RtuRegisterFullProtocolShadowImpl implements RtuRegisterFullProtocolShadow{

    private ObisCode registerObisCode;
    private int rtuRegisterId;
    private Unit registerUnit;

    public ObisCode getRegisterObisCode() {
        return registerObisCode;
    }

    public void setRegisterObisCode(final ObisCode registerObisCode) {
        this.registerObisCode = registerObisCode;
    }

    public int getRtuRegisterId() {
        return rtuRegisterId;
    }

    public void setRtuRegisterId(final int rtuRegisterid) {
        this.rtuRegisterId = rtuRegisterid;
    }

    public Unit getRegisterUnit() {
        return registerUnit;
    }

    public void setRegisterUnit(final Unit registerUnit) {
        this.registerUnit = registerUnit;
    }
}
