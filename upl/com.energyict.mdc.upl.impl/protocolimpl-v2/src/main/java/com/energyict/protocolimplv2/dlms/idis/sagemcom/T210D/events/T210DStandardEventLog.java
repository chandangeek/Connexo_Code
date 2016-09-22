package com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimplv2.dlms.idis.am130.events.AM130StandardEventLog;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by cisac on 6/30/2016.
 */
public class T210DStandardEventLog extends AM130StandardEventLog {

    public T210DStandardEventLog(TimeZone timeZone, DataContainer dc) {
        super(timeZone, dc);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 236:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Certificate almost expired"));
                break;
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}
