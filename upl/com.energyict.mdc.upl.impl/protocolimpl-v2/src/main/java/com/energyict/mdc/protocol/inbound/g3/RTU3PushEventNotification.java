package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.RTU3;

/**
 * Does pretty much the same as the PushEventNotification of the G3 gateway,
 * but uses the RTU3 protocol to connect to the DC device.
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 17/06/2015 - 11:33
 */
public class RTU3PushEventNotification extends PushEventNotification {

    //TODO: support events from the meter too
    //TODO junit test with trace from Alex

    protected DeviceProtocol newGatewayProtocol() {
        return new RTU3();
    }

    protected DlmsSession getDlmsSession(DeviceProtocol gatewayProtocol) {
        return ((RTU3) gatewayProtocol).getDlmsSession();
    }
}