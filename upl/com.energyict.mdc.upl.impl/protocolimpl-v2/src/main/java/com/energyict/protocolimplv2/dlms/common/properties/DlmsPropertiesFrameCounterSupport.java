package com.energyict.protocolimplv2.dlms.common.properties;

import com.energyict.dlms.cosem.FrameCounterProvider;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import java.math.BigDecimal;

/**
 * This class shall be used to pass all what is needed in order to create/init a dlms session with public client
 */
public abstract class DlmsPropertiesFrameCounterSupport extends DlmsProperties {

    public ObisCode frameCounterObisCode() {
        return FrameCounterProvider.getDefaultObisCode();
    }

    public BigDecimal getPublicClientMacAddress() {
        return BigDecimal.valueOf(getProperties().getTypedProperty(DlmsProtocolProperties.PUBLIC_CLIENT_MAC_ADDRESS, DlmsProtocolProperties.DEFAULT_CLIENT_MAC_ADDRESS));
    }

    public abstract boolean useCachedFrameCounter();
}
