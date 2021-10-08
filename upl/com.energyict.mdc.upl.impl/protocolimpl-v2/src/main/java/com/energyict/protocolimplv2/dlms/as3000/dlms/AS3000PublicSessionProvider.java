package com.energyict.protocolimplv2.dlms.as3000.dlms;

import com.energyict.mdc.protocol.ComChannel;

import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.dlms.as3000.properties.AS3000Properties;
import com.energyict.protocolimplv2.dlms.common.dlms.PublicClientDlmsSessionProvider;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;

import java.math.BigDecimal;
import java.util.logging.Logger;

public class AS3000PublicSessionProvider implements PublicClientDlmsSessionProvider {

    private final Logger logger;
    private final ComChannel comChannel;
    private final HHUSignOnV2 hhuSignOn;

    public AS3000PublicSessionProvider(ComChannel comChannel, HHUSignOnV2 hhuSignOn, Logger logger) {
        this.comChannel = comChannel;
        this.logger = logger;
        this.hhuSignOn = hhuSignOn;
    }

    @Override
    public DlmsSession provide() {
        AS3000Properties properties = new AS3000Properties();
        BigDecimal publicClientMacAddress = properties.getPublicClientMacAddress();
        properties.getProperties().setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, publicClientMacAddress);
        properties.setSecurityPropertySet(new DeviceProtocolSecurityPropertySetImpl(publicClientMacAddress, 0, 0, 0, 0, 0, properties.getProperties()));
        return new AS3000DlmsSession(comChannel, properties, hhuSignOn, logger);
    }
}
