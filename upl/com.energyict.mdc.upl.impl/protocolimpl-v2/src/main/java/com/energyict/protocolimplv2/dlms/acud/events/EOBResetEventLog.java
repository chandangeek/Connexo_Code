package com.energyict.protocolimplv2.dlms.acud.events;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.protocol.MeterEvent;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class EOBResetEventLog extends AbstractEvent {

    public EOBResetEventLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId, DataStructure evStructure) {
        switch (eventId) {
            case 1:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.EOB_RESET, eventId, "EOB reset event"));
                break;
            default:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}