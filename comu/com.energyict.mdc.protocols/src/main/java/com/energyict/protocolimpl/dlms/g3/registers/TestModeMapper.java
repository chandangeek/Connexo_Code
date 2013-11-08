package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Data;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.AS330D;

import java.io.IOException;

/**
* Copyrights EnergyICT
* Date: 22/03/12
* Time: 9:22
*/
class TestModeMapper extends G3Mapping {

    public TestModeMapper(ObisCode obisCode) {
        super(obisCode);
    }

    @Override
    public RegisterValue readRegister(AS330D as330D) throws IOException {
        final CosemObjectFactory cof = as330D.getSession().getCosemObjectFactory();
        final Data data = cof.getData(getObisCode());
        final BooleanObject valueAttr = data.getValueAttr(BooleanObject.class);
        final String textValue = valueAttr.getState() ? "TEST_MODE" : "NORMAL_MODE";
        return new RegisterValue(getObisCode(), textValue);
    }

}
