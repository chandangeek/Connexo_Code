package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;

public class BitStringMapper implements AttributeMapper<BitString> {

    @Override
    public RegisterValue map(AbstractDataType attribute, ObisCode obisCode) throws MappingException {
        BitString bitString = attribute.getBitString();
        if (attribute.isBitString() && bitString != null) {
            return new RegisterValue(obisCode, bitString.toString());
        } else {
            throw new MappingException("Developed obis code:" + obisCode + " as an bit string yet reading tells otherwise");
        }
    }

    @Override
    public Class<BitString> dataType() {
        return BitString.class;
    }
}
