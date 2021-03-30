package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Unsigned64;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;

public class U64Maper implements AttributeMapper<Unsigned64> {

    private final Unit unit;

    public U64Maper(Unit unit) {
        this.unit = unit;
    }

    @Override
    public RegisterValue map(AbstractDataType attribute, OfflineRegister offlineRegister) throws MappingException {
        Unsigned64 unsigned64 = attribute.getUnsigned64();
        if (attribute.isUnsigned64() && unsigned64 != null) {
            return new RegisterValue(offlineRegister, new Quantity(unsigned64.toBigDecimal(), unit));
        } else {
            throw new MappingException("Developed obis code:" + offlineRegister.getObisCode() + " as an unsigned64 yet reading tells otherwise");
        }
    }

    @Override
    public Class<Unsigned64> dataType() {
        return Unsigned64.class;
    }
}
