package com.energyict.protocolimplv2.eict.rtu3.beacon3100.logbooks;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by iulian on 7/26/2016.
 */
public class Beacon3100DisconnectorControlLog extends Beacon3100AbstractEventLog {

    public Beacon3100DisconnectorControlLog(DataContainer dc, TimeZone timeZone) {
        super(dc, timeZone);
    }

    @Override
    protected String getLogBookName() {
        return "Disconnector control log";
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int dlmsCode, int deviceCode, String message) {
        int eiCode = MeterEvent.OTHER;
        String eventDescription = getDefaultEventDescription(dlmsCode, deviceCode, message);

        switch (deviceCode) {


            case 255:
                eiCode = MeterEvent.EVENT_LOG_CLEARED;
                eventDescription = getLogBookName() + " cleared";
                break;
            default:
                // just the defaults

        }

        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), eiCode, deviceCode, eventDescription));
    }
}
