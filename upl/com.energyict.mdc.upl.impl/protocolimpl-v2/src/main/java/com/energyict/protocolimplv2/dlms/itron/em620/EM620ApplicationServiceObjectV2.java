package com.energyict.protocolimplv2.dlms.itron.em620;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.XdlmsAse;
import com.energyict.dlms.protocolimplv2.ApplicationServiceObjectV2;

public class EM620ApplicationServiceObjectV2 extends ApplicationServiceObjectV2 {

    public EM620ApplicationServiceObjectV2(XdlmsAse xDlmsAse, ProtocolLink protocolLink, SecurityContext securityContext, int contextId, byte[] calledAPTitle, byte[] calledAEQualifier, byte[] callingAEQualifier) {
        super(xDlmsAse, protocolLink, securityContext, contextId, calledAPTitle, calledAEQualifier, callingAEQualifier);
    }

    @Override
    public void releaseAssociation() {
    }

    @Override
    protected void silentDisconnect() {
        try {
            protocolLink.getDLMSConnection().disconnectMAC();
        } catch (Exception e) {
            // Absorb exception
        }
    }
}
