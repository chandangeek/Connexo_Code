package com.energyict.protocolimplv2.dlms.acud.events.electric;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimplv2.dlms.acud.events.AbstractEvent;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DisconnectorEventLog extends AbstractEvent {

    public DisconnectorEventLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId, DataStructure evStructure) {
        switch (eventId) {
            case 1:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.DISCONNECTOR_STATUS_CHANGED, eventId, "Disconnector status changed"));
                break;
            default:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}