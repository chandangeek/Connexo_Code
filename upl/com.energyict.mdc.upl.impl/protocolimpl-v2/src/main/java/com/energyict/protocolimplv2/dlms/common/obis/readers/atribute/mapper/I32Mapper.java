package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Integer32;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;

public class I32Mapper implements AttributeMapper<Integer32>{

    private final Unit unit;

    public I32Mapper(Unit unit) {
        this.unit = unit;
    }

    @Override
    public RegisterValue map(AbstractDataType attribute, ObisCode obisCode) throws MappingException {
        Integer32 integer32 = attribute.getInteger32();
        if (attribute.isInteger32() && integer32 != null) {
            return new RegisterValue(obisCode, new Quantity(integer32.toBigDecimal(), unit));
        } else {
            throw new MappingException("Developed obis code:" + obisCode + " as an int32 string yet reading tells otherwise");
        }
    }

    @Override
    public Class<Integer32> dataType() {
        return Integer32.class;
    }
}
