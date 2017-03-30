/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.idis.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MBusControlLog2 extends AbstractEvent {

    public MBusControlLog2(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 170:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MANUAL_DISCONNECTION_MBUS, eventId, "Manual disconnection M-Bus channel 2"));
                break;
            case 171:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MANUAL_CONNECTION_MBUS, eventId, "Manual connection M-Bus channel 2"));
                break;
            case 172:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_DISCONNECTION_MBUS, eventId, "Remote disconnection M-Bus channel 2"));
                break;
            case 173:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_CONNECTION_MBUS, eventId, "Remote connection M-Bus channel 2"));
                break;
            case 174:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.VALVE_ALARM_MBUS, eventId, "Valve alarm M-Bus channel 2"));
                break;
            case 255:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.EVENT_LOG_CLEARED, eventId, "MBus control 2 event log cleared"));
                break;
            default:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}