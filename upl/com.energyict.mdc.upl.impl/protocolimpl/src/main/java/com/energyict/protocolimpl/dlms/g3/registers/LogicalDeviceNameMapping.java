package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Data;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.AS330D;

import java.io.IOException;

/**
* Copyrights EnergyICT
* Date: 22/03/12
* Time: 9:23
*/
class LogicalDeviceNameMapping extends G3Mapping {

    public LogicalDeviceNameMapping(ObisCode obisCode) {
        super(obisCode);
    }

    @Override
    public RegisterValue readRegister(AS330D as330D) throws IOException {
        final CosemObjectFactory cof = as330D.getSession().getCosemObjectFactory();
        final Data data = cof.getData(getObisCode());
        final OctetString valueAttr = data.getValueAttr(OctetString.class);
        final String textValue = valueAttr.stringValue();
        return new RegisterValue(getObisCode(), textValue);
    }
}
