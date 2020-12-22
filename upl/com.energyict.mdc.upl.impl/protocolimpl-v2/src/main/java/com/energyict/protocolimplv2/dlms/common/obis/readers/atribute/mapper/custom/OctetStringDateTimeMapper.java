package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper.custom;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper.AttributeMapper;

import java.util.TimeZone;

public class OctetStringDateTimeMapper implements AttributeMapper<OctetString> {


    private final TimeZone timeZone;

    public OctetStringDateTimeMapper(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public RegisterValue map(AbstractDataType attribute, ObisCode obisCode) {

        return new RegisterValue(obisCode, attribute.getOctetString().getDateTime(timeZone).toString());
    }

    @Override
    public Class<OctetString> dataType() {
        return OctetString.class;
    }
}
