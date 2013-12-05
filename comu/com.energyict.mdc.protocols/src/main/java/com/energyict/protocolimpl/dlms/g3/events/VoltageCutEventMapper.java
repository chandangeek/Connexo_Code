package com.energyict.protocolimpl.dlms.g3.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

/**
 * Copyrights EnergyICT
 * Date: 28/03/12
 * Time: 15:32
 */
public class VoltageCutEventMapper extends G3EventMapper {

    public static final EventDescription[] EVENT_DESCRIPTIONS = new EventDescription[]{
            new EventDescription(0, MeterEvent.EVENT_LOG_CLEARED, "Voltage cut log cleared"),
            new EventDescription(51, MeterEvent.PHASE_FAILURE, "Phase 1 power failure"),
            new EventDescription(52, MeterEvent.OTHER, "Phase 1 power restored"),
            new EventDescription(53, MeterEvent.PHASE_FAILURE, "Phase 2 power failure"),
            new EventDescription(54, MeterEvent.OTHER, "Phase 2 power restored"),
            new EventDescription(55, MeterEvent.PHASE_FAILURE, "Phase 3 power failure"),
            new EventDescription(56, MeterEvent.OTHER, "Phase 3 power restored")
    };

    @Override
    protected EventDescription[] getEventDescriptions() {
        return EVENT_DESCRIPTIONS;
    }
}