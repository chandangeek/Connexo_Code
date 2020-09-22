package com.energyict.protocolimplv2.dlms.acud.events.electric;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimplv2.dlms.acud.events.AbstractEvent;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class QualityEventLog extends AbstractEvent {

    public QualityEventLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId, DataStructure evStructure) {
        switch (eventId) {
            case 1:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MAXIMUM_CURRENT, eventId, "Maximum current"));
                break;
            case 2:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SAG_CONFIRMED, eventId, "Sag confirmed"));
                break;
            case 3:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SWELL_CONFIRMED, eventId, "Swell confirmed"));
                break;
            default:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}