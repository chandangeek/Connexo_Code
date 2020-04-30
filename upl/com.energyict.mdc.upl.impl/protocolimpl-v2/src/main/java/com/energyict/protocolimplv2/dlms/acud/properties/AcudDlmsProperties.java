package com.energyict.protocolimplv2.dlms.acud.properties;


import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

public class AcudDlmsProperties extends DlmsProperties {

    @Override
    public byte[] getSystemIdentifier() {
        return null;
    }

    public boolean isReadCache() {
        return getProperties().<Boolean>getTypedProperty(DlmsProtocolProperties.READCACHE_PROPERTY, false);
    }
}