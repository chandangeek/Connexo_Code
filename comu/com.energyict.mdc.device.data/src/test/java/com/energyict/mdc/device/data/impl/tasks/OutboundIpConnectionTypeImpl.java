package com.energyict.mdc.device.data.impl.tasks;


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
