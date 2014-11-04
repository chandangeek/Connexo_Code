package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.dynamic.PropertySpecService;

/**
 * Copyrights EnergyICT
 * Date: 6/19/14
 * Time: 5:00 PM
 */
public class InboundIpConnectionTypeImpl extends IpConnectionType {

    public InboundIpConnectionTypeImpl(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public Direction getDirection() {
        return Direction.INBOUND;
    }

}
