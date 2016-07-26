package com.energyict.protocolimplv2.eict.rtu3.beacon3100.logbooks;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by iulian on 7/26/2016.
 */
public class Beacon3100VoltageEventLog extends Beacon3100AbstractEventLog {

    public Beacon3100VoltageEventLog(DataContainer dc, TimeZone timeZone) {
        super(dc, timeZone);
    }

    @Override
    protected String getLogBookName() {
        return "Voltage event log";
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int dlmsCode, int deviceCode, String message) {
        int eiCode = MeterEvent.OTHER;
        String eventDescription = getDefaultEventDescription(dlmsCode, deviceCode, message);

        switch (dlmsCode) {
            case 0x0080: eiCode = MeterEvent.OTHER; eventDescription =  " POWER_MANAGEMENT_SWITCH_LOW_POWER"; break;
            case 0x0081: eiCode = MeterEvent.OTHER; eventDescription =  " POWER_MANAGEMENT_SWITCH_FULL_POWER"; break;
            case 0x0082: eiCode = MeterEvent.OTHER; eventDescription =  " POWER_MANAGEMENT_SWITCH_REDUCED_POWER"; break;
            case 0x0083: eiCode = MeterEvent.OTHER; eventDescription =  " POWER_MANAGEMENT_MAINS_LOST"; break;
            case 0x0084: eiCode = MeterEvent.OTHER; eventDescription =  " POWER_MANAGEMENT_MAINS_RECOVERED"; break;
            case 0x0085: eiCode = MeterEvent.OTHER; eventDescription =  " POWER_MANAGEMENT_LAST_GASP"; break;
            case 0x0086: eiCode = MeterEvent.OTHER; eventDescription =  " POWER_MANAGEMENT_BATTERY_CHARGE_START"; break;
            case 0x0087: eiCode = MeterEvent.OTHER; eventDescription =  " POWER_MANAGEMENT_BATTERY_CHARGE_STOP"; break;


            case 255:
                eiCode = MeterEvent.EVENT_LOG_CLEARED;
                eventDescription = getLogBookName() + " cleared";
                break;
            default:
                // just the defaults

        }

        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), eiCode, dlmsCode, eventDescription));
    }
}
