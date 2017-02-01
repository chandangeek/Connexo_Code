/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.connection;

import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComPort;

import org.json.JSONException;
import org.json.JSONWriter;

/**
 * Represents a {@link com.energyict.mdc.engine.events.ConnectionEvent}
 * for a connection with a {@link com.energyict.mdc.protocol.api.device.BaseDevice device} that was closed.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-06 (13:20)
 */
public class CloseConnectionEvent extends AbstractConnectionEventImpl {

    public CloseConnectionEvent(ServiceProvider serviceProvider, ComPort comPort, ConnectionTask connectionTask) {
        super(serviceProvider, connectionTask, comPort);
    }

    @Override
    public boolean isClosed () {
        return true;
    }

    @Override
    protected void toString (JSONWriter writer) throws JSONException {
        super.toString(writer);
        writer.key("closing").value(Boolean.TRUE);
    }

}