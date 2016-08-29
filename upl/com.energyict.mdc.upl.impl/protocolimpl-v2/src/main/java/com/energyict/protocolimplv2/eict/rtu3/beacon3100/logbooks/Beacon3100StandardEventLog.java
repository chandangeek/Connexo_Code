package com.energyict.protocolimplv2.eict.rtu3.beacon3100.logbooks;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 */

public class Beacon3100StandardEventLog extends Beacon3100AbstractEventLog {
    public Beacon3100StandardEventLog(DataContainer dc, TimeZone timeZone) {
        super(dc, timeZone);
    }

    @Override
    protected String getLogBookName() {
        return "Standard event log";
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int dlmsCode, int deviceCode, String message) {
        /*int eiCode = MeterEvent.OTHER;*/
        String eventDescription = null;

        if (deviceCode == EVENT_LOG_CLEARED_DEVICECODE){
            /*eiCode = MeterEvent.EVENT_LOG_CLEARED;*/
            eventDescription = getLogBookName() + " cleared";
        } else {
            eventDescription = getEventInfo(dlmsCode, deviceCode);

            if (eventDescription == null) {
                eventDescription = getDefaultEventDescription(dlmsCode, deviceCode, message);
            }
        }
        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), dlmsCode, deviceCode, "Event: "+eventDescription+" - "+message));
    }
}
