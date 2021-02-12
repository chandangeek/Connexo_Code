package com.energyict.protocolimplv2.dlms.as3000.properties;

import com.energyict.dlms.cosem.FrameCounterProvider;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.dlms.common.properties.DlmsPropertiesFrameCounterSupport;
import com.energyict.protocolimplv2.nta.abstractnta.NTASecurityProvider;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import java.math.BigDecimal;

import static com.energyict.dlms.common.DlmsProtocolProperties.READCACHE_PROPERTY;
import static com.energyict.dlms.common.DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS;

public class AS3000Properties extends DlmsPropertiesFrameCounterSupport {


    public ObisCode frameCounterObisCode() {
        return FrameCounterProvider.getDefaultObisCode();
    }

    public boolean useCachedFrameCounter() {
        return getProperties().getTypedProperty(AS3000ConfigurationSupport.USE_CACHED_FRAME_COUNTER, false);
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (securityProvider == null) {
            securityProvider = new NTASecurityProvider(getProperties(), getSecurityPropertySet().getAuthenticationDeviceAccessLevel());
        }
        return securityProvider;
    }

    @Override
    public int getServerLowerMacAddress() {
        return parseBigDecimalProperty(SERVER_LOWER_MAC_ADDRESS, BigDecimal.valueOf(85));
    }

    public boolean flushCachedObjectList() {
        return getProperties().<Boolean>getTypedProperty(READCACHE_PROPERTY, false);
    }

    public int getMaxDaysToReadLoadProfile() {
        return getProperties().getTypedProperty(
                AS3000ConfigurationSupport.LIMIT_MAX_NR_OF_DAYS_PROPERTY,
                BigDecimal.valueOf(30)
        ).intValue();
    }
}
