/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

public class DataValueMapping extends G3Mapping {

    private final Unit unit;

    public DataValueMapping(ObisCode obisCode) {
        this(obisCode, Unit.get(""));
    }

    public DataValueMapping(ObisCode obisCode, Unit unit) {
        super(obisCode);
        this.unit = unit;
    }

    @Override
    public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
        final Data data = cosemObjectFactory.getData(getObisCode());
        return parse(data.getValueAttr(), unit);
    }

    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        final BigDecimal value = BigDecimal.valueOf(abstractDataType.longValue());
        final Quantity quantityValue = new Quantity(value, this.unit);
        return new RegisterValue(getObisCode(), quantityValue);
    }

    @Override
    public int getDLMSClassId() {
        return DLMSClassId.DATA.getClassId();
    }
}
