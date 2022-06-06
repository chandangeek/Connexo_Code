package com.energyict.protocolimplv2.dlms.ei7;

import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.protocolimplv2.dlms.a2.A2DlmsSession;
import com.energyict.protocolimplv2.dlms.a2.A2HHUSignOn;

import java.util.logging.Logger;

public class EI7DlmsSession extends A2DlmsSession {

    public EI7DlmsSession(ComChannel comChannel, DlmsSessionProperties properties) {
        super(comChannel, properties);
    }

    public EI7DlmsSession(ComChannel comChannel, DlmsSessionProperties properties, Logger logger) {
        super(comChannel, properties, logger);
    }

    public EI7DlmsSession(ComChannel comChannel, DlmsSessionProperties properties, A2HHUSignOn hhuSignOn, String deviceId) {
        super(comChannel, properties, hhuSignOn, deviceId);
    }
}
