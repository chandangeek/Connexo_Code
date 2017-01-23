package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.util.Date;

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
    public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
        final Data data = cosemObjectFactory.getData(getObisCode());
        return parse(data.getValueAttr(BooleanObject.class));
    }

    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        final String textValue = ((BooleanObject) abstractDataType).getState() ? "TEST_MODE" : "NORMAL_MODE";
        return new RegisterValue(getObisCode(), textValue);
}

    @Override
    public int getDLMSClassId() {
        return DLMSClassId.DATA.getClassId();
    }
}