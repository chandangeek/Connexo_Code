package com.energyict.mdc.device.data.rest.impl;

/**
 * Copyrights EnergyICT
 * Date: 6/19/14
 * Time: 4:51 PM
 */
public class InboundNoParamsConnectionTypeImpl extends NoParamsConnectionType {

    @Override
    public Direction getDirection() {
        return Direction.INBOUND;
    }

}