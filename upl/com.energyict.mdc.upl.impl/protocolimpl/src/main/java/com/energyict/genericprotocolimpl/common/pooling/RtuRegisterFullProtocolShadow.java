package com.energyict.genericprotocolimpl.common.pooling;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;

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
