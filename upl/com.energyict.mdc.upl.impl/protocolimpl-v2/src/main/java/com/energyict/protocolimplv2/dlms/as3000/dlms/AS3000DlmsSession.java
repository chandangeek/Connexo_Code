package com.energyict.protocolimplv2.dlms.as3000.dlms;

import com.energyict.mdc.protocol.ComChannel;

import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.dlms.protocolimplv2.connection.DlmsV2Connection;
import com.energyict.dlms.protocolimplv2.connection.HDLCConnection;

import java.util.logging.Logger;

public class AS3000DlmsSession extends DlmsSession {


    public AS3000DlmsSession(ComChannel comChannel, DlmsSessionProperties properties, HHUSignOnV2 hhuSignOn, Logger logger) {
        super(comChannel, properties, hhuSignOn, "");
    }

    @Override
    protected DlmsV2Connection defineTransportDLMSConnection() {
        return new HDLCConnection(super.getComChannel(), getProperties());
    }

}
