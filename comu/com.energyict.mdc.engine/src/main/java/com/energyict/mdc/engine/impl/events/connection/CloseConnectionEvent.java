package com.energyict.mdc.engine.impl.events.connection;

import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.model.ComPort;

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

    /**
     * For the externalization process only.
     *
     * @param serviceProvider The ServiceProvider
     */
    public CloseConnectionEvent (ServiceProvider serviceProvider) {
        super(serviceProvider);
    }

    public CloseConnectionEvent (ComPort comPort, ConnectionTask connectionTask, ServiceProvider serviceProvider) {
        super(connectionTask, comPort, serviceProvider);
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