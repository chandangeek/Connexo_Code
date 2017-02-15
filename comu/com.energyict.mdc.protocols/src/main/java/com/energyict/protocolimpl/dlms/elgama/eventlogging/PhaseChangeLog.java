/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.elgama.eventlogging;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class PhaseChangeLog extends AbstractEvent {

    private static final int EVENT_PHASE_L1_CONNECTED = 0x01;
    private static final int EVENT_PHASE_L2_CONNECTED = 0x02;
    private static final int EVENT_PHASE_L3_CONNECTED = 0x04;

    public PhaseChangeLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {

        previousSize = meterEvents.size();
        this.meterEvents = meterEvents;


        if ((eventId & EVENT_PHASE_L1_CONNECTED) != 0) {
            eventTimeStamp = fixStamp(eventTimeStamp);
            meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, EVENT_PHASE_L1_CONNECTED, "Phase L1 connected"));
        }
        if ((eventId & EVENT_PHASE_L2_CONNECTED) != 0) {
            eventTimeStamp = fixStamp(eventTimeStamp);
            meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, EVENT_PHASE_L2_CONNECTED, "Phase L2 connected"));
        }
        if ((eventId & EVENT_PHASE_L3_CONNECTED) != 0) {
            eventTimeStamp = fixStamp(eventTimeStamp);
            meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, EVENT_PHASE_L3_CONNECTED, "Phase L3 connected"));
        }
        if (!anEventWasAdded()) {
            meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Unknown eventcode:" + eventId));
        }
    }
}
