package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;

public class U32Mapper implements AttributeMapper<Unsigned32> {

    private final Unit unit;

    public U32Mapper(Unit unit) {
        this.unit = unit;
    }

    @Override
    public RegisterValue map(AbstractDataType attribute, ObisCode obisCode) throws MappingException {
        Unsigned32 unsigned32 = attribute.getUnsigned32();
        if (attribute.isUnsigned32() && unsigned32 != null) {
            return new RegisterValue(obisCode, new Quantity(unsigned32.toBigDecimal(), unit));
        } else {
            throw new MappingException("Developed obis code:" + obisCode + " as an unsigned32 string yet reading tells otherwise");
        }
    }

    @Override
    public Class<Unsigned32> dataType() {
        return Unsigned32.class;
    }
}
