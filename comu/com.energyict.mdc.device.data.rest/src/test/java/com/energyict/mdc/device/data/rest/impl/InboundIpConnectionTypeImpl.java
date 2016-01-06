package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.dynamic.PropertySpecService;

import javax.inject.Inject;

/**
 * Copyrights EnergyICT
 * Date: 6/19/14
 * Time: 5:00 PM
 */
public class InboundIpConnectionTypeImpl extends IpConnectionType {

    @Inject
    public InboundIpConnectionTypeImpl(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public Direction getDirection() {
        return Direction.INBOUND;
    }

}