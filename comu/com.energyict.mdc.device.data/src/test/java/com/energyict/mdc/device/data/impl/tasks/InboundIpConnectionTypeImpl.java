package com.energyict.mdc.device.data.impl.tasks;


import com.energyict.mdc.dynamic.PropertySpec;

import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 6/19/14
 * Time: 5:00 PM
 */
public class InboundIpConnectionTypeImpl extends IpConnectionType {

    @Override
    public Direction getDirection() {
        return Direction.INBOUND;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }
}
