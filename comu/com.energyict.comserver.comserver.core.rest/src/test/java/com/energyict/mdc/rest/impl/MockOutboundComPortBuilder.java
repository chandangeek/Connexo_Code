/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl;

import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;

public class MockOutboundComPortBuilder implements OutboundComPort.OutboundComPortBuilder {

    @Override
    public OutboundComPort.OutboundComPortBuilder comPortType(ComPortType comPortType) {
        return this;
    }

    @Override
    public OutboundComPort.OutboundComPortBuilder active(boolean active) {
        return this;
    }

    @Override
    public OutboundComPort.OutboundComPortBuilder description(String description) {
        return this;
    }

    @Override
    public OutboundComPort add() {
        return null;
    }
}
