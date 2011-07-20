package com.energyict.smartmeterprotocolimpl.eict.ukhub.messaging;

import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.dlms.common.DlmsSession;

/**
 * Copyrights EnergyICT
 * Date: 20-jul-2011
 * Time: 17:15:18
 */
public class UkHubMessageExecutor {

    private final AbstractSmartDlmsProtocol protocol;
    private final DlmsSession dlmsSession;


    public UkHubMessageExecutor(final AbstractSmartDlmsProtocol protocol) {
        this.protocol = protocol;
        this.dlmsSession = this.protocol.getDlmsSession();
    }
}
