package com.energyict.protocolimplv2.dlms.as253;

import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.dlms.protocolimplv2.connection.DlmsV2Connection;
import com.energyict.dlms.protocolimplv2.connection.HDLCConnection;
import com.energyict.mdc.protocol.ComChannel;

public class A1253DlmsSession extends DlmsSession {

    A1253DlmsSession(ComChannel comChannel, DlmsSessionProperties properties, HHUSignOnV2 hhuSignOn, String deviceId) {
        super(comChannel, properties, hhuSignOn, deviceId);
    }

    /**
    * Set connection mode to HDLC (A1800 supports only HDLC connection)
    *
    * */
    @Override
    protected DlmsV2Connection defineTransportDLMSConnection() {
        return new HDLCConnection(getComChannel(), getProperties());
    }
}
