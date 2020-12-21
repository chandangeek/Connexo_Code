package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.VisibleString;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;

public class VisibleStringMapper implements AttributeMapper<VisibleString> {

    @Override
    public RegisterValue map(AbstractDataType attribute, ObisCode obisCode) throws MappingException {
        VisibleString visibleString = attribute.getVisibleString();
        if (attribute.isVisibleString() && visibleString != null) {
            return new RegisterValue(obisCode, visibleString.getStr());
        } else {
            throw new MappingException("Developed obis code:" + obisCode + " as an visible string yet reading tells otherwise");
        }
    }

    @Override
    public Class<VisibleString> dataType() {
        return VisibleString.class;
    }
}
