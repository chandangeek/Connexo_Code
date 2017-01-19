package com.energyict.mdc.device.data.impl.tasks;

/**
 * Copyrights EnergyICT
 * Date: 6/19/14
 * Time: 4:51 PM
 */
public class InboundNoParamsConnectionTypeImpl extends NoParamsConnectionType {

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.INBOUND;
    }
}
