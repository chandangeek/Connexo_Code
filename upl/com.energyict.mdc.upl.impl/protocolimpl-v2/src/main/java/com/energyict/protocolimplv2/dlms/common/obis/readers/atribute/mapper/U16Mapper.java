package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;

public class U16Mapper implements AttributeMapper<Unsigned16> {

    private final Unit unit;

    public U16Mapper(Unit unit) {
        this.unit = unit;
    }

    @Override
    public RegisterValue map(AbstractDataType attribute, OfflineRegister offlineRegister) throws MappingException {
        Unsigned16 unsigned16 = attribute.getUnsigned16();
        if (attribute.isUnsigned16() && unsigned16 != null) {
            return new RegisterValue(offlineRegister, new Quantity(unsigned16.toBigDecimal(), unit));
        } else {
            throw new MappingException("Developed obis code:" + offlineRegister.getObisCode() + " as an unsigned16 yet reading tells otherwise");
        }
    }

    @Override
    public Class<Unsigned16> dataType() {
        return Unsigned16.class;
    }
}
