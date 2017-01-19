package com.energyict.mdc.device.topology.impl;

/**
 * Copyrights EnergyICT
 * Date: 6/19/14
 * Time: 4:51 PM
 */
public class OutboundNoParamsConnectionTypeImpl extends NoParamsConnectionType {

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.OUTBOUND;
    }

}
