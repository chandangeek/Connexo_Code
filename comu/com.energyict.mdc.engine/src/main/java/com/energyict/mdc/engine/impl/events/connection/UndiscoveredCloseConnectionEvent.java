package com.energyict.mdc.engine.impl.events.connection;

import com.energyict.mdc.engine.model.InboundComPort;

import org.json.JSONException;
import org.json.JSONWriter;

/**
 * Represents a {@link com.energyict.mdc.engine.events.ConnectionEvent}
 * for an inbound connection that was closed
 * but it was not possible to discover which device
 * was actually connecting to the InboundComPort.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-07 (11:30)
 */
public class UndiscoveredCloseConnectionEvent extends UndiscoveredConnectionEvent {

    /**
     * For the externalization process only.
     */
    public UndiscoveredCloseConnectionEvent() {
        super();
    }

    public UndiscoveredCloseConnectionEvent(InboundComPort comPort) {
        super(comPort);
    }

    @Override
    public boolean isEstablishing () {
        return false;
    }

    @Override
    public boolean isClosed () {
        return true;
    }

    @Override
    protected void toString (JSONWriter writer) throws JSONException {
        super.toString(writer);
        writer.key("close").value(Boolean.TRUE);
    }

}