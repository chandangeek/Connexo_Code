package com.energyict.protocolimplv2.dlms.itron.em620;

import com.energyict.mdc.protocol.ComChannel;

import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.dlms.protocolimplv2.HdlcDlmsSession;
import com.energyict.dlms.protocolimplv2.connection.DlmsV2Connection;
import com.energyict.dlms.protocolimplv2.connection.HDLCConnection;

public class EM620DlmsSession extends HdlcDlmsSession {
    public EM620DlmsSession(ComChannel comChannel, DlmsSessionProperties properties) {
        super(comChannel, properties);
    }

    @Override
    protected DlmsV2Connection defineTransportDLMSConnection() {
        return new HDLCConnection(getComChannel(), getProperties(), false);
    }

    @Override
    public void disconnect() {
        if (getDLMSConnection() != null) {
            getDlmsV2Connection().disconnectMAC();
        }
    }

    @Override
    protected EM620ApplicationServiceObjectV2 buildAso(String calledSystemTitleString) {
        return new EM620ApplicationServiceObjectV2(
                buildXDlmsAse(),
                this,
                buildSecurityContext(),
                getContextId(),
                calledSystemTitleString == null ? null : calledSystemTitleString.getBytes(),
                null,
                getCallingAEQualifier()
        );
    }
}
