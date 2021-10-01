package com.energyict.protocolimplv2.dlms.idis.AS3000G.events;

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
                break;
            default:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}