/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.dlms.DefaultDLMSMeterEventMapper;

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