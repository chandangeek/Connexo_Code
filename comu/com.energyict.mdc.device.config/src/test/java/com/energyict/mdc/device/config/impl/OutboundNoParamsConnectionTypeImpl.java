package com.energyict.mdc.device.config.impl;

/**
 * Copyrights EnergyICT
 * Date: 6/17/14
 * Time: 11:37 AM
 */
public class OutboundNoParamsConnectionTypeImpl extends NoParamsConnectionType {

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.OUTBOUND;
    }

}