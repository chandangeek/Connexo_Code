/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;

import java.io.IOException;
import java.util.Date;

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