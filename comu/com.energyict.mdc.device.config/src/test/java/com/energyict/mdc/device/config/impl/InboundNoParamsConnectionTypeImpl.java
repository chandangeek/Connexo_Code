package com.energyict.mdc.device.config.impl;

/**
 * Copyrights EnergyICT
 * Date: 6/17/14
 * Time: 11:37 AM
 */
public class InboundNoParamsConnectionTypeImpl extends NoParamsConnectionType {

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.INBOUND;
    }

}