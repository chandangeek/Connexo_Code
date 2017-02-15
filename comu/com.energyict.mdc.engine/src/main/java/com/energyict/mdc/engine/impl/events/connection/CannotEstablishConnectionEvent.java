/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.connection;

import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.protocol.api.ConnectionException;

import org.json.JSONException;
import org.json.JSONWriter;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Represents a {@link com.energyict.mdc.engine.events.ConnectionEvent}
 * for an outbound connection that failed to established for a {@link com.energyict.mdc.protocol.api.device.BaseDevice device}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-06 (11:30)
 */
public class CannotEstablishConnectionEvent extends AbstractConnectionEventImpl {

    private String failureMessage;

    public CannotEstablishConnectionEvent(ServiceProvider serviceProvider, ComPort comPort, ConnectionTask connectionTask, ConnectionException cause) {
        super(serviceProvider, connectionTask, comPort);
        this.copyFailureMessageFromException(cause);
    }

    private void copyFailureMessageFromException (ConnectionException cause) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        writer.println(cause.getMessage());
        cause.printStackTrace(writer);
        this.failureMessage = stringWriter.toString();
    }

    @Override
    public boolean isFailure () {
        return true;
    }

    @Override
    public String getFailureMessage () {
        return this.failureMessage;
    }

    @Override
    protected void toString (JSONWriter writer) throws JSONException {
        super.toString(writer);
        writer.key("failure").value(this.getFailureMessage());
    }

}