package com.energyict.protocolimplv2.dlms.acud.events.electric;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimplv2.dlms.acud.events.AbstractEvent;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class CurrentEventLog extends AbstractEvent {

    public CurrentEventLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId, DataStructure evStructure) {
        switch (eventId) {
            case 1:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.RECOVERY_AFTER_CURRENT_OVERLIMIT, eventId, "Auto recovery after current overlimit"));
                break;
            case 2:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.RECOVERY_TIMES_SETTING_CHANGED, eventId, "Auto recovery times setting changed"));
                break;
            case 3:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.RECOVERY_MECHANISM_RELEASED, eventId, "Auto recovery mechanism released"));
                break;
            default:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}