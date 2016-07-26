package com.energyict.protocolimplv2.eict.rtu3.beacon3100.logbooks;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by iulian on 7/26/2016.
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
        int eiCode = MeterEvent.OTHER;
        String eventDescription = getDefaultEventDescription(dlmsCode, deviceCode, message);

        switch (deviceCode) {
            case 0x0000:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Other";
                break;
            case 0x0001:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Power down";
                break;
            case 0x0002:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Power up";
                break;
            case 0x0003:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Watchdog reset";
                break;
            case 0x0004:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Before clock set";
                break;
            case 0x0005:
                eiCode = MeterEvent.OTHER;
                eventDescription = " After clock set";
                break;
            case 0x0006:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Clock set";
                break;
            case 0x0007:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Configuration change";
                break;
            case 0x0008:
                eiCode = MeterEvent.OTHER;
                eventDescription = " RAM memory error";
                break;
            case 0x0009:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Program flow error";
                break;
            case 0x000A:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Register overflow";
                break;
            case 0x000B:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Fatal error";
                break;
            case 0x000C:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Clear data";
                break;
            case 0x000D:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Hardware error";
                break;
            case 0x000E:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Meter alarm";
                break;
            case 0x000F:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Rom memory error";
                break;
            case 0x0010:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Maximum demand reset";
                break;
            case 0x0011:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Billing action";
                break;
            case 0x0012:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Application alert start";
                break;
            case 0x0013:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Application alert stop";
                break;
            case 0x0014:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Phase failure";
                break;
            case 0x0017:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Tamper detected";
                break;
            case 0x0018:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Meter cover opened";
                break;
            case 0x0019:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Terminal cover opened";
                break;
            case 0x001A:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Reverse Run";
                break;
            case 0x001B:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Load Profile cleared";
                break;
            case 0x001C:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Event log cleared";
                break;
            case 0x001D:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Daylight saving time enabled or disabled";
                break;
            case 0x001E:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Clock invalid";
                break;
            case 0x001F:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Replace Battery";
                break;
            case 0x0020:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Battery voltage low";
                break;
            case 0x0021:
                eiCode = MeterEvent.OTHER;
                eventDescription = " TOU activated";
                break;
            case 0x0022:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Error register cleared";
                break;
            case 0x0023:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Alarm register cleared";
                break;
            case 0x0024:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Program memory error";
                break;
            case 0x0025:
                eiCode = MeterEvent.OTHER;
                eventDescription = " NV memory error";
                break;
            case 0x0026:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Watchdog error";
                break;
            case 0x0027:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Measurement system error";
                break;
            case 0x0028:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Firmware ready for activation";
                break;
            case 0x0029:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Firmware activated";
                break;
            case 0x002A:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Terminal cover closed";
                break;
            case 0x002B:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Strong DC field detected";
                break;
            case 0x002C:
                eiCode = MeterEvent.OTHER;
                eventDescription = " No strong DC field anymore";
                break;
            case 0x002D:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Meter cover closed";
                break;
            case 0x002E:
                eiCode = MeterEvent.OTHER;
                eventDescription = " n times wrong password";
                break;
            case 0x002F:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Manual disconnection";
                break;
            case 0x0030:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Manual connection";
                break;
            case 0x0031:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Remote disconnection";
                break;
            case 0x0032:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Remote connection";
                break;
            case 0x0033:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Local disconnection";
                break;
            case 0x0034:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Limiter threshold exceeded";
                break;
            case 0x0035:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Limiter threshold ok";
                break;
            case 0x0036:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Limiter threshold changed";
                break;
            case 0x0037:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Communication error MBus";
                break;
            case 0x0038:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Communication ok M-Bus";
                break;
            case 0x0039:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Replace Battery M-Bus";
                break;
            case 0x003A:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Fraud attempt M-Bus";
                break;
            case 0x003B:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Clock adjusted M-Bus";
                break;
            case 0x003C:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Manual disconnection M-Bus";
                break;
            case 0x003D:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Manual connection M-Bus";
                break;
            case 0x003E:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Remote disconnection MBus";
                break;
            case 0x003F:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Remote connection MBus";
                break;
            case 0x0040:
                eiCode = MeterEvent.OTHER;
                eventDescription = " Valve alarm M-Bus";
                break;

            case 255:
                eiCode = MeterEvent.EVENT_LOG_CLEARED;
                eventDescription = getLogBookName() + " cleared";
                break;
            default:
                // just the defaults

        }

        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), eiCode, dlmsCode, eventDescription+" "+message));
    }
}
