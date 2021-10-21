package com.energyict.protocolimplv2.dlms.idis.as3000g.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.dlms.idis.events.DisconnectorControlLog;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AS3000DisconnectorControlLog extends DisconnectorControlLog {

    public AS3000DisconnectorControlLog(TimeZone timeZone, DataContainer dc, boolean isMirrorConnection) {
        super(timeZone, dc, isMirrorConnection);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 236:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.METER_RECORDS_CONTRACTOR_CHANGED_TO_ARMED_STATUS, eventId, "Meter records when the contactor is changed to the armed status"));
                break;
            default:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}