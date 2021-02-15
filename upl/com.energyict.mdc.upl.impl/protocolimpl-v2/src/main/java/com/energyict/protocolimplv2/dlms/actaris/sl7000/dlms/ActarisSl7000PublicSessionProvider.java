package com.energyict.protocolimplv2.dlms.actaris.sl7000.dlms;

import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.properties.ActarisSl7000Properties;
import com.energyict.protocolimplv2.dlms.common.dlms.PublicClientDlmsSessionProvider;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;

import java.math.BigDecimal;
import java.util.logging.Logger;

public class ActarisSl7000PublicSessionProvider implements PublicClientDlmsSessionProvider {

    private final Logger logger;
    private final ComChannel comChannel;

    public ActarisSl7000PublicSessionProvider(ComChannel comChannel, Logger logger) {
        this.comChannel = comChannel;
        this.logger = logger;
    }

    @Override
    public DlmsSession provide() {
        ActarisSl7000Properties properties = new ActarisSl7000Properties();
        BigDecimal publicClientMacAddress = properties.getPublicClientMacAddress();
        properties.getProperties().setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, publicClientMacAddress);
        properties.setSecurityPropertySet(new DeviceProtocolSecurityPropertySetImpl(publicClientMacAddress, 0,0,0,0,0, properties.getProperties()));
        return new DlmsSession(comChannel, properties, logger);
    }
}
