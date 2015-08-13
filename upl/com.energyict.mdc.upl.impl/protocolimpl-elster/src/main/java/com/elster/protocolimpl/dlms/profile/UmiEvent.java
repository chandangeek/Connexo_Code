package com.elster.protocolimpl.dlms.profile;

import com.energyict.protocol.MeterEvent;

/**
 * User: heuckeg
 * Date: 18.04.13
 * Time: 08:16
 */
public class UmiEvent
{
    private final int umiEventCode;
    private final int eisEventCode;
    private final String msg;

    private UmiEvent(final int umiEventCode, final int eisEventCode, final String msg)
    {
        this.umiEventCode = umiEventCode;
        this.eisEventCode = eisEventCode;
        this.msg = msg;
    }

    public int getUmiEventCode()
    {
        return umiEventCode;
    }

    public int getEisEventCode()
    {
        return eisEventCode;
    }

    public String getMsg()
    {
        return msg;
    }

    public static UmiEvent getUmiEvent(final int umiEvent, final byte[] data)
    {
        int eisEvent = MeterEvent.OTHER;
        String msg = "";
        for (byte b: data)
        {
            msg += String.format(" $%02x", b);
        }

        switch (umiEvent)
        {
            case 0:
                eisEvent = MeterEvent.CONFIGURATIONCHANGE;
                msg = "Verification Object Write (" + msg + ")";
                break;
            case 1:
                eisEvent = MeterEvent.REVERSE_RUN;
                msg = "Reverse run";
                break;
            case 2:
                eisEvent = MeterEvent.METER_ALARM;
                msg = "Software Upgrade (" + msg + ")";
                break;
            case 3:
                eisEvent = MeterEvent.CONFIGURATIONCHANGE;
                msg = "Configuration Upgrade (" + msg + ")";
                break;
            case 4:
                msg = "Miser Mode (" + msg + ")";
                break;
            case 5:
                eisEvent = MeterEvent.VALVE_ALARM_MBUS;
                msg = "Valve Released (" + msg + ")";
                break;
            case 6:
                eisEvent = MeterEvent.VALVE_ALARM_MBUS;
                msg = "Valve Closed (" + msg + ")";
                break;
            case 7:
                eisEvent = MeterEvent.VALVE_ALARM_MBUS;
                msg = "Valve Release Fault (" + msg + ")";
                break;
            case 8:
                eisEvent = MeterEvent.VALVE_ALARM_MBUS;
                msg = "Valve Close Fault (" + msg + ")";
                break;
            case 9:
                eisEvent = MeterEvent.METER_ALARM;
                msg = "Battery (" + msg + ")";
                break;
            case 10:
                eisEvent = MeterEvent.COVER_OPENED;
                msg = "Case opened";
                break;
            case 11:
                eisEvent = MeterEvent.METER_COVER_CLOSED;
                msg = "Case Closed";
                break;
            case 12:
                eisEvent = MeterEvent.METER_ALARM;
                msg = "Magnet on (" + msg + ")";
                break;
            case 13:
                eisEvent = MeterEvent.METER_ALARM;
                msg = "Magnet off (" + msg + ")";
                break;
            case 14:
                eisEvent = MeterEvent.METER_ALARM;
                msg = "UMI control (" + msg + ")";
                break;
            case 15:
                msg = "Spare (" + msg + ")";
                break;
            case 16:
                eisEvent = MeterEvent.METER_ALARM;
                msg = "Software restart (" + msg + ")";
                break;
            case 17:
                eisEvent = MeterEvent.METER_ALARM;
                msg = "Opto communication (" + msg + ")";
                break;
            case 18:
                eisEvent = MeterEvent.SETCLOCK;
                msg = "Clock set (" + msg + ")";
                break;
            case 19:
                msg = "Leaky value";
                break;
            case 20:
                eisEvent = MeterEvent.METER_ALARM;
                msg = "UMI control (" + msg + ")";
                break;
            case 21:
                eisEvent = MeterEvent.MEASUREMENT_SYSTEM_ERROR;
                msg = "Environmental conditions (" + msg + ")";
                break;
            case 22:
                eisEvent = MeterEvent.PROGRAM_FLOW_ERROR;
                msg = "Software alarm (" + msg + ")";
                break;
            case 23:
                eisEvent = MeterEvent.REGISTER_OVERFLOW;
                msg = "Event logbook 90% full";
                break;
            case 24:
                eisEvent = MeterEvent.EVENT_LOG_CLEARED;
                msg = "Event erase (" + msg + ")";
                break;
            case 25:
                eisEvent = MeterEvent.N_TIMES_WRONG_PASSWORD;
                msg = "Failed authentication";
                break;
            case 26:
                msg = "Spare (" + msg + ")";
                break;
            case 27:
                eisEvent = MeterEvent.METER_ALARM;
                msg = "Self test failure (" + msg + ")";
                break;
            case 28:
                eisEvent = MeterEvent.METER_ALARM;
                msg = "Bad peripheral (" + msg + ")";
                break;
            case 29:
                eisEvent = MeterEvent.METER_ALARM;
                msg = "Peripheral found";
                break;
            case 30:
                eisEvent = MeterEvent.METER_ALARM;
                msg = "Bad decrypt";
                break;
            default:
                msg = "Spare (" + msg + ")";
        }
        return new UmiEvent(umiEvent, eisEvent, msg);
    }
}
