/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-09 (15:17)
 */
public class DummyInboundConnectionType1 extends DummyConnectionType {
    @Override
    public Direction getDirection() {
        return Direction.INBOUND;
    }
}