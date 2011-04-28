package com.energyict.genericprotocolimpl.common.pooling;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;

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
