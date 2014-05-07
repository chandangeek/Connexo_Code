package com.energyict.mdc.engine.impl.events.connection;

import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.model.EngineModelService;
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
     */
    public CloseConnectionEvent (Clock clock, DeviceDataService deviceDataService, EngineModelService engineModelService) {
        super(clock, deviceDataService, engineModelService);
    }

    public CloseConnectionEvent (ComPort comPort, ConnectionTask connectionTask, Clock clock, DeviceDataService deviceDataService, EngineModelService engineModelService) {
        super(connectionTask, comPort, clock, deviceDataService, engineModelService);
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