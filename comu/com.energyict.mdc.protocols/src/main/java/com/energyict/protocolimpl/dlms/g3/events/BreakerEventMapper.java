/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

public class BreakerEventMapper extends G3EventMapper {

    public static final EventDescription[] EVENT_DESCRIPTIONS = new EventDescription[]{
            new EventDescription(0, MeterEvent.EVENT_LOG_CLEARED, "Breaker log cleared"),
            new EventDescription(1, MeterEvent.REMOTE_DISCONNECTION, "Opening the breaker by remote command the from head end"),
            new EventDescription(2, MeterEvent.LOCAL_DISCONNECTION, "Opening the breaker due to a surge"),
            new EventDescription(3, MeterEvent.LOCAL_DISCONNECTION, "Opening the breaker due to an overpower"),
            new EventDescription(4, MeterEvent.LOCAL_DISCONNECTION, "Opening the breaker for scheduled load shedding"),
            new EventDescription(5, MeterEvent.LOCAL_DISCONNECTION, "Opening the breaker due to overheating, below the maximum switching current"),
            new EventDescription(6, MeterEvent.LOCAL_DISCONNECTION, "Opening the breaker due to overheating, above the maximum switching current"),
            new EventDescription(11, MeterEvent.REMOTE_CONNECTION, "Closing the breaker by remote command from the head end"),
            new EventDescription(12, MeterEvent.MANUAL_CONNECTION, "Closing the breaker by manual action on device"),
            new EventDescription(13, MeterEvent.OTHER, "Closing the breaker at the end of the scheduled load shedding"),
            new EventDescription(14, MeterEvent.MANUAL_CONNECTION, "Closing the breaker by manual action on the breaker")
    };

    @Override
    protected EventDescription[] getEventDescriptions() {
        return EVENT_DESCRIPTIONS;
    }
}