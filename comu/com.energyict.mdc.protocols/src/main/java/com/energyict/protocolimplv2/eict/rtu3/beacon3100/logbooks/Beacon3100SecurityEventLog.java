package com.energyict.protocolimplv2.eict.rtu3.beacon3100.logbooks;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 */

public class Beacon3100SecurityEventLog extends Beacon3100AbstractEventLog {

    public Beacon3100SecurityEventLog(DataContainer dc, TimeZone timeZone) {
        super(dc, timeZone);
    }

    @Override
    protected String getLogBookName() {
        return "Security event log";
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int dlmsCode, int deviceCode, String message) {
        /*int eiCode = MeterEvent.OTHER;*/
        int eiCode = dlmsCode;
        String eventDescription = null;

        if (deviceCode == EVENT_LOG_CLEARED_DEVICECODE){
            eiCode = MeterEvent.EVENT_LOG_CLEARED;
            eventDescription = getLogBookName() + " cleared";
        } else {
            eventDescription = getEventInfo(dlmsCode, deviceCode);

            if (eventDescription == null) {
                eventDescription = getDefaultEventDescription(dlmsCode, deviceCode, message);
            }
        }
        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), eiCode, deviceCode, "Event: "+eventDescription+" - "+message));
    }
}
