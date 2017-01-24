package com.energyict.protocolimpl.dlms.g3.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

/**
 * Copyrights EnergyICT
 * Date: 28/03/12
 * Time: 15:32
 */
public class CommunicationEventMapper extends G3EventMapper {

    public static final EventDescription[] EVENT_DESCRIPTIONS = new EventDescription[]{
            new EventDescription(0, MeterEvent.EVENT_LOG_CLEARED, "Communication log cleared"),
            new EventDescription(29, MeterEvent.OTHER, "Failed to change CC_LAN key"),
            new EventDescription(30, MeterEvent.OTHER, "Failed to change CC_LOCAL key"),
            new EventDescription(31, MeterEvent.OTHER, "Start of an application association in Euridis interface"),
            new EventDescription(32, MeterEvent.OTHER, "End of an application association in Euridis interface"),
            new EventDescription(33, MeterEvent.OTHER, "Error on attempted application association in Euridis interface"),
            new EventDescription(34, MeterEvent.OTHER, "Start of application association on Read/Write client in PLC interface"),
            new EventDescription(35, MeterEvent.OTHER, "End of application association on Read/Write client in PLC interface"),
            new EventDescription(36, MeterEvent.OTHER, "Error attempting application association on Read/Write client in PLC interface"),
            new EventDescription(37, MeterEvent.OTHER, "Start of application association in broadcast client in PLC interface"),
            new EventDescription(38, MeterEvent.OTHER, "End of application association in broadcast client in PLC interface"),
            new EventDescription(39, MeterEvent.OTHER, "Error attempting application association in broadcast client in PLC interface")
    };

    @Override
    protected EventDescription[] getEventDescriptions() {
        return EVENT_DESCRIPTIONS;
    }
}