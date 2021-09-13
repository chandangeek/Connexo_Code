package com.energyict.protocolimplv2.edp;

import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.dlms.protocolimplv2.connection.DlmsV2Connection;
import com.energyict.dlms.protocolimplv2.connection.HDLCConnection;
import com.energyict.mdc.protocol.ComChannel;

public class EDPDlmsSession extends DlmsSession {

    public EDPDlmsSession(ComChannel comChannel, DlmsSessionProperties properties) {
        super(comChannel, properties);
    }

    @Override
    protected DlmsV2Connection defineTransportDLMSConnection() {
        return new HDLCConnection(getComChannel(), getProperties());
    }
}
