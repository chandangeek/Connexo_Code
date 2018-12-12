/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.connection;

import com.energyict.mdc.engine.config.InboundComPort;

import org.json.JSONException;
import org.json.JSONWriter;

/**
 * Represents a {@link com.energyict.mdc.engine.events.ConnectionEvent}
 * for an inbound connection that was established
 * but it is yet undiscovered which device
 * is actually connecting to the InboundComPort.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-07 (11:34)
 */
public class UndiscoveredEstablishConnectionEvent extends UndiscoveredConnectionEvent {

    public UndiscoveredEstablishConnectionEvent(ServiceProvider serviceProvider, InboundComPort comPort) {
        super(serviceProvider, comPort);
    }

    @Override
    public boolean isEstablishing () {
        return true;
    }

    @Override
    public boolean isClosed () {
        return false;
    }

    @Override
    protected void toString (JSONWriter writer) throws JSONException {
        super.toString(writer);
        writer.key("establishing").value(Boolean.TRUE);
    }

}