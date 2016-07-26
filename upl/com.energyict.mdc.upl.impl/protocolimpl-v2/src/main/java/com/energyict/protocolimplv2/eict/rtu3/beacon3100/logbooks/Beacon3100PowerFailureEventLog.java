package com.energyict.protocolimplv2.eict.rtu3.beacon3100.logbooks;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;

import java.util.*;

/**
 * Created by iulian on 7/26/2016.
 */
public class Beacon3100PowerFailureEventLog  extends Beacon3100AbstractEventLog {

    public Beacon3100PowerFailureEventLog(DataContainer dc, TimeZone timeZone) {
        super(dc, timeZone);
    }

    @Override
    protected String getLogBookName() {
        return "Power failure event log";
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
