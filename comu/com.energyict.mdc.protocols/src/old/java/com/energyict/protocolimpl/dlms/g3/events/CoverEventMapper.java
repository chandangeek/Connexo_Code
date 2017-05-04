/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

public class CoverEventMapper extends G3EventMapper {

    public static final EventDescription[] EVENT_DESCRIPTIONS = new EventDescription[]{
            new EventDescription(0, MeterEvent.EVENT_LOG_CLEARED, "Cover log cleared"),
            new EventDescription(21, MeterEvent.TERMINAL_OPENED, "Terminal cover opened"),
            new EventDescription(22, MeterEvent.TERMINAL_COVER_CLOSED, "Terminal cover closed")
    };

    @Override
    protected EventDescription[] getEventDescriptions() {
        return EVENT_DESCRIPTIONS;
    }
}