package com.energyict.mdc.pluggable.rest.impl;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-09 (15:15)
 */
public class DummyOutboundConnectionType1 extends DummyConnectionType {
    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.OUTBOUND;
    }
}