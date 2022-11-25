package com.energyict.protocolimplv2.dlms.landisAndGyr;

import com.energyict.mdc.protocol.ComChannel;

import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.dlms.protocolimplv2.HdlcDlmsSession;
import com.energyict.dlms.protocolimplv2.connection.DlmsV2Connection;
import com.energyict.dlms.protocolimplv2.connection.HDLCConnection;

public class ZMYDlmsSession extends HdlcDlmsSession {

    public ZMYDlmsSession(ComChannel comChannel, DlmsSessionProperties properties) {
        super(comChannel, properties);
    }

    @Override
    protected DlmsV2Connection defineTransportDLMSConnection() {
        return new HDLCConnection(getComChannel(), getProperties(), false);
    }
}