/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.dynamic.PropertySpecService;

import javax.inject.Inject;

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
