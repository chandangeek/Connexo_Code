package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;

public class U8Mapper implements AttributeMapper<Unsigned8> {

    private final Unit unit;

    public U8Mapper(Unit unit) {
        this.unit = unit;
    }

    @Override
    public RegisterValue map(AbstractDataType attribute, OfflineRegister offlineRegister) throws MappingException {
        Unsigned8 unsigned8 = attribute.getUnsigned8();
        if (attribute.isUnsigned8() && unsigned8 != null) {
            return new RegisterValue(offlineRegister, new Quantity(unsigned8.toBigDecimal(), unit));
        } else {
            throw new MappingException("Developed obis code:" + offlineRegister.getObisCode() + " as an unsigned8 yet reading tells otherwise");
        }
    }

    @Override
    public Class<Unsigned8> dataType() {
        return Unsigned8.class;
    }
}
