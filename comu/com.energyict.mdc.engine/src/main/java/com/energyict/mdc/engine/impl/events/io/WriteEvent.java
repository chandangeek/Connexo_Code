package com.energyict.mdc.engine.impl.events.io;

import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.impl.core.inbound.ComPortRelatedComChannel;
import com.energyict.mdc.engine.model.EngineModelService;

import java.util.Date;

/**
 * Models an event that represent a write session for a {@link ComPortRelatedComChannel}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-12 (12:24)
 */
public class WriteEvent extends CommunicationEventImpl {

    /**
     * For the externalization process only.
     */
    public WriteEvent (Clock clock, DeviceDataService deviceDataService, EngineModelService engineModelService) {
        super(clock, deviceDataService, engineModelService);
    }

    public WriteEvent (Date occurrenceTimestamp, ComPort comPort, byte[] bytes, Clock clock, DeviceDataService deviceDataService, EngineModelService engineModelService) {
        super(occurrenceTimestamp, comPort, bytes, clock, deviceDataService, engineModelService);
    }

    @Override
    public boolean isWrite () {
        return true;
    }

}