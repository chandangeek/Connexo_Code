/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-09 (15:15)
 */
public class DummyOutboundConnectionType2 extends DummyConnectionType {
    @Override
    public Direction getDirection() {
        return Direction.OUTBOUND;
    }
}