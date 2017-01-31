/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.elgama.eventlogging;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class OverVoltageLog extends AbstractEvent {

    private static final int EVENT_OVER_VOLTAGE_ALL = 0x00;
    private static final int EVENT_OVER_VOLTAGE_L1 = 0x01;
    private static final int EVENT_OVER_VOLTAGE_L2 = 0x02;
    private static final int EVENT_OVER_VOLTAGE_L3 = 0x04;

    public OverVoltageLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        previousSize = meterEvents.size();
        this.meterEvents = meterEvents;

        if (eventId == EVENT_OVER_VOLTAGE_ALL) {
            eventTimeStamp = fixStamp(eventTimeStamp);
            meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, EVENT_OVER_VOLTAGE_ALL, "Over-voltage event end in all three phases"));
        }
        if ((eventId & EVENT_OVER_VOLTAGE_L1) != 0) {
            eventTimeStamp = fixStamp(eventTimeStamp);
            meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, EVENT_OVER_VOLTAGE_L1, "Over-voltage event in phase L1"));
        }
        if ((eventId & EVENT_OVER_VOLTAGE_L2) != 0) {
            eventTimeStamp = fixStamp(eventTimeStamp);
            meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, EVENT_OVER_VOLTAGE_L2, "Over-voltage event in phase L2"));
        }
        if ((eventId & EVENT_OVER_VOLTAGE_L3) != 0) {
            eventTimeStamp = fixStamp(eventTimeStamp);
            meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, EVENT_OVER_VOLTAGE_L3, "Over-voltage event in phase L3"));
        }
        if (!anEventWasAdded()) {
            meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode:" + eventId));
        }
    }
}