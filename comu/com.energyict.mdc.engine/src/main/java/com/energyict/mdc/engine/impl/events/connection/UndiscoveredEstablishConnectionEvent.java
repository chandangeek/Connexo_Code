package com.energyict.mdc.engine.impl.events.connection;

import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPort;
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

    /**
     * For the externalization process only.
     */
    public UndiscoveredEstablishConnectionEvent (Clock clock, DeviceDataService deviceDataService, EngineModelService engineModelService) {
        super(clock, deviceDataService, engineModelService);
    }

    public UndiscoveredEstablishConnectionEvent (InboundComPort comPort, Clock clock, DeviceDataService deviceDataService, EngineModelService engineModelService) {
        super(comPort, clock, deviceDataService, engineModelService);
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