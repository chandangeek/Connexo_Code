/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

public class OutboundNoParamsConnectionTypeImpl extends NoParamsConnectionType {

    @Override
    public Direction getDirection() {
        return Direction.OUTBOUND;
    }

}
