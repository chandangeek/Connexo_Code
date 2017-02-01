/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

public class OutboundNoParamsConnectionTypeImpl extends NoParamsConnectionType {

    @Override
    public Direction getDirection() {
        return Direction.OUTBOUND;
    }

}