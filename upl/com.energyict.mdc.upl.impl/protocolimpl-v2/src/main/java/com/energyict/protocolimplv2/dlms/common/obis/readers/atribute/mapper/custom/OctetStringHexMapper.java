package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper.custom;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper.AttributeMapper;

public class OctetStringHexMapper implements AttributeMapper<OctetString> {

    public OctetStringHexMapper() {
    }

    @Override
    public RegisterValue map(AbstractDataType attribute, OfflineRegister offlineRegister) {
        return new RegisterValue(offlineRegister, ProtocolUtils.outputHexString(attribute.getOctetString().getOctetStr()));
    }

    @Override
    public Class<OctetString> dataType() {
        return OctetString.class;
    }
}
