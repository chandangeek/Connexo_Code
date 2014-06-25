package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.protocol.api.ConnectionType;

/**
 * Copyrights EnergyICT
 * Date: 6/19/14
 * Time: 5:00 PM
 */
public class OutboundIpConnectionTypeImpl extends IpConnectionType {

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.OUTBOUND;
    }
}
