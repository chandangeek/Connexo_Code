/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.elgama.eventlogging;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class PowerFailureLog extends AbstractEvent {

    private static final int EVENT_POWER_OUTAGE = 0x01;
    private static final int EVENT_POWER_STARTUP = 0x00;

    public PowerFailureLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case EVENT_POWER_OUTAGE:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.POWERDOWN, eventId, "Power outage"));
                break;
            case EVENT_POWER_STARTUP:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.POWERUP, eventId, "Power start-up"));
                break;
            default:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}