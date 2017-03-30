/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.idis.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class PowerFailureEventLog extends AbstractEvent {

    public PowerFailureEventLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.VOLTAGE_SAG, eventId, "Long power failure in a phase, duration: " + eventId + " seconds"));
    }
}