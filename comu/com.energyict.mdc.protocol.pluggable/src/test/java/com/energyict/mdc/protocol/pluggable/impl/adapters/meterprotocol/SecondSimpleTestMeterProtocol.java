/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

public class SecondSimpleTestMeterProtocol extends SimpleTestMeterProtocol {

    @Override
    public String getProtocolDescription() {
        return this.getClass().getName();
    }

    public SecondSimpleTestMeterProtocol() {
        super();
    }
}
