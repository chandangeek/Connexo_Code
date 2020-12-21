package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Integer32;
import com.energyict.dlms.axrdencoding.Integer64;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;

public class I64Mapper implements AttributeMapper<Integer64>{

    private final Unit unit;

    public I64Mapper(Unit unit) {
        this.unit = unit;
    }

    @Override
    public RegisterValue map(AbstractDataType attribute, ObisCode obisCode) throws MappingException {
        Integer64 i = attribute.getInteger64();
        if (attribute.isInteger64() && i != null) {
            return new RegisterValue(obisCode, new Quantity(i.toBigDecimal(), unit));
        } else {
            throw new MappingException("Developed obis code:" + obisCode + " as an int32 string yet reading tells otherwise");
        }
    }

    @Override
    public Class<Integer64> dataType() {
        return Integer64.class;
    }
}
