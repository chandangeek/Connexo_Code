package com.energyict.protocolimplv2.dlms.idis.AS3000G.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.dlms.idis.events.StandardEventLog;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AS3000GStandardEventLog extends StandardEventLog {

    public AS3000GStandardEventLog(TimeZone timeZone, DataContainer dc, boolean isMirrorConnection) {
        super(timeZone, dc);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 7:
                break; // block 7th event because AS3000G not support it
            case 52:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.UNEXPECTED_CONSUMPTION, eventId, "Unexpected consumption"));
                break;
            case 200:
                break;
            case 203:
                break;
            case 204:
                break;
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}