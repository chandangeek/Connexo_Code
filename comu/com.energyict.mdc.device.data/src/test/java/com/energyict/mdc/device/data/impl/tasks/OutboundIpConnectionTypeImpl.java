package com.energyict.mdc.device.data.impl.tasks;


import com.energyict.mdc.dynamic.PropertySpecService;

/**
 * Copyrights EnergyICT
 * Date: 6/19/14
 * Time: 5:00 PM
 */
public class OutboundIpConnectionTypeImpl extends IpConnectionType {

    public OutboundIpConnectionTypeImpl(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public Direction getDirection() {
        return Direction.OUTBOUND;
    }

}