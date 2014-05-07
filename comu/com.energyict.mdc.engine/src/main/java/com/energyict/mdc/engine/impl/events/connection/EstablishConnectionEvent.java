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
 * for a connection that was established for or by a  device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-06 (10:46)
 */
public class EstablishConnectionEvent extends AbstractConnectionEventImpl {

    /**
     * For the externalization process only.
     */
    public EstablishConnectionEvent (Clock clock, DeviceDataService deviceDataService, EngineModelService engineModelService) {
        super(clock, deviceDataService, engineModelService);
    }

    public EstablishConnectionEvent (ComPort comPort, ConnectionTask connectionTask, Clock clock, DeviceDataService deviceDataService, EngineModelService engineModelService) {
        super(connectionTask, comPort, clock, deviceDataService, engineModelService);
    }

    @Override
    public boolean isEstablishing () {
        return true;
    }

    @Override
    protected void toString (JSONWriter writer) throws JSONException {
        super.toString(writer);
        writer.key("establishing").value(Boolean.TRUE);
    }

}