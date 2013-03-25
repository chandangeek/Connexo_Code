package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.cosem.Register;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.AS330D;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 22/03/12
 * Time: 8:31
 */
public class RegisterMapping extends G3Mapping {

    public RegisterMapping(ObisCode obis) {
        super(obis);
    }

    @Override
    public RegisterValue readRegister(AS330D as330D) throws IOException {
        final Register register = as330D.getSession().getCosemObjectFactory().getRegister(getObisCode());
        final Quantity quantityValue = register.getQuantityValue();
        return new RegisterValue(getObisCode(), quantityValue);
    }

}
