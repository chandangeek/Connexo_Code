package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;

public class ArrayMapper implements AttributeMapper<Array> {

    @Override
    public RegisterValue map(AbstractDataType attribute, ObisCode obisCode) throws MappingException {
        Array array = attribute.getArray();
        if (attribute.isArray() && array != null) {
            return new RegisterValue(obisCode, array.toString());
        } else {
            throw new MappingException("Developed obis code:" + obisCode + " as an array yet reading tells otherwise");
        }
    }

    @Override
    public Class<Array> dataType() {
        return Array.class;
    }
}
