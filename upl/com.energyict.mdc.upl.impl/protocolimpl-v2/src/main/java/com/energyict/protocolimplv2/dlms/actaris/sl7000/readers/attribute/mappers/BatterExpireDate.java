package com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.attribute.mappers;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper.AttributeMapper;

public class BatterExpireDate implements AttributeMapper<OctetString> {
    @Override
    public RegisterValue map(AbstractDataType attribute, OfflineRegister offlineRegister) {
        OctetString data = attribute.getOctetString();
        String info = data.getOctetStr()[3] + "/" + data.getOctetStr()[2] + "/" +
                (short) (((data.getOctetStr()[0] & 0xFF) << 8) | (data.getOctetStr()[1] & 0xFF));
        return new RegisterValue(offlineRegister, info);
    }

    @Override
    public Class<OctetString> dataType() {
        return null;
    }
}
