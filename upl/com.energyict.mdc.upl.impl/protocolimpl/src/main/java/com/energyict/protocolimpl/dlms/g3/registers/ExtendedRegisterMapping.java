package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.AS330D;

import java.io.IOException;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 22/03/12
 * Time: 9:05
 */
public class ExtendedRegisterMapping extends G3Mapping {

    public ExtendedRegisterMapping(ObisCode obis) {
        super(obis);
    }

    @Override
    public RegisterValue readRegister(AS330D as330D) throws IOException {
        final ExtendedRegister extendedRegister = as330D.getSession().getCosemObjectFactory().getExtendedRegister(getObisCode());
        final Quantity quantityValue = extendedRegister.getQuantityValue();
        final Date captureTime = extendedRegister.getCaptureTime();
        return new RegisterValue(getObisCode(), quantityValue, captureTime);
    }
}
