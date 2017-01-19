package com.energyict.mdc.device.data.impl.tasks;


import com.energyict.mdc.dynamic.PropertySpecService;

import javax.inject.Inject;

/**
 * Copyrights EnergyICT
 * Date: 6/19/14
 * Time: 5:00 PM
 */
public class OutboundIpConnectionTypeImpl extends IpConnectionType {

    @Inject
    public OutboundIpConnectionTypeImpl(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.OUTBOUND;
    }

}