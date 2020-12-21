package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;

public class OctetStringMapper implements AttributeMapper<OctetString> {

    @Override
    public RegisterValue map(AbstractDataType attribute, ObisCode obisCode) throws MappingException {
        OctetString octetString = attribute.getOctetString();
        if (attribute.isOctetString() && octetString != null) {
            return new RegisterValue(obisCode, octetString.stringValue());
        } else {
            throw new MappingException("Developed obis code:" + obisCode + " as an octet string yet reading tells otherwise");
        }
    }

    @Override
    public Class<OctetString> dataType() {
        return OctetString.class;
    }
}
