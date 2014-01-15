package com.energyict.genericprotocolimpl.common.pooling;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.Register;

/**
 * Contains logical information about an {@link Register}
 */
public interface RtuRegisterFullProtocolShadow {

    ObisCode getRegisterObisCode();
    void setRegisterObisCode(ObisCode obisCode);

    int getRtuRegisterId();
    void setRtuRegisterId(int rtuRegisterId);

    Unit getRegisterUnit();
    void setRegisterUnit(Unit registerUnit);
}
