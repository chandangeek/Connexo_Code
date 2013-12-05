package com.energyict.genericprotocolimpl.common.pooling;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;

/**
 * Contains logical information about an {@link com.energyict.mdw.amr.Register}
 */
public interface RtuRegisterFullProtocolShadow {

    ObisCode getRegisterObisCode();
    void setRegisterObisCode(ObisCode obisCode);

    int getRtuRegisterId();
    void setRtuRegisterId(int rtuRegisterId);

    Unit getRegisterUnit();
    void setRegisterUnit(Unit registerUnit);
}
