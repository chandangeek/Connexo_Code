package com.energyict.protocolimpl.dlms.g3.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocolimpl.dlms.DefaultDLMSMeterEventMapper;

/**
 * Copyrights EnergyICT
 * Date: 28/03/12
 * Time: 15:03
 */
public abstract class G3EventMapper extends DefaultDLMSMeterEventMapper {

    protected abstract EventDescription[] getEventDescriptions();

    public final int getEisEventCode(int meterEventCode) {
        return findEventDescription(meterEventCode).getEisEventCode();
    }

    public final String getEventMessage(int meterEventCode) {
        return findEventDescription(meterEventCode).getEventMessage();
    }

    private final EventDescription findEventDescription(int meterEventCode) {
        EventDescription[] eventDescriptions = getEventDescriptions();
        for (EventDescription description : eventDescriptions) {
            if (description.getMeterEventCode() == meterEventCode) {
                return description;
            }
        }
        return new EventDescription(meterEventCode, MeterEvent.OTHER, "Unknown event [" + meterEventCode + "]");
    }
}