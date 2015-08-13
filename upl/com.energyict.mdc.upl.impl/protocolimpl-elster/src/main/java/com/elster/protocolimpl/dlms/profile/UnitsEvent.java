package com.elster.protocolimpl.dlms.profile;

import com.energyict.protocol.MeterEvent;

/**
 * User: heuckeg
 * Date: 30.08.13
 * Time: 13:53
 */
public enum UnitsEvent
{
    Event1(2, MeterEvent.EVENT_LOG_CLEARED, "UNI-TS Event Log reset"),
    Event2(8, MeterEvent.BILLING_ACTION, "New Activity Calendar: Activation"),
    Event3(9, MeterEvent.BILLING_ACTION, "New Activity Calendar: Download"),
    Event4(10, MeterEvent.METER_ALARM, "Clock synchronization failed"),
    Event5(11, MeterEvent.SETCLOCK, "Clock synchronization non-regulatory-relevant"),
    Event6(12, MeterEvent.SETCLOCK, "Clock synchronization regulatory-relevant"),
    Event7(20, MeterEvent.PROGRAM_FLOW_ERROR, "Conversion - Volume conversion algorithm failure: start"),
    Event8(22, MeterEvent.APPLICATION_ALERT_START, "Device general failure: start"),
    Event9(26, MeterEvent.METER_ALARM, "UNI-TS Event Log full"),
    Event10(27, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "UNI-TS Event Log ≥ 90%Full"),
    Event11(40, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Gas flow-rate physical above maximum accepted value(out of physical working range): start"),
    Event12(41, MeterEvent.LIMITER_THRESHOLD_OK, "Gas flow-rate physical above maximum accepted value(out of physical working range): end"),
    Event13(42, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Gas flow-rate physical below minimum accepted value(out of physical working range): start - Reverse flow detected"),
    Event14(52, MeterEvent.HARDWARE_ERROR, "Gas pressure broken: start"),
    Event15(53, MeterEvent.OTHER, "Gas pressure broken: end"),
    Event16(62, MeterEvent.HARDWARE_ERROR, "Gas temperature broken: start"),
    Event17(63, MeterEvent.OTHER, "Gas temperature broken: end"),
    Event18(66, MeterEvent.NV_MEMORY_ERROR, "Memory failure"),
    Event19(67, MeterEvent.BILLING_ACTION, "Mode-Device mode (normal, not_configured, maintenance) changed"),
    Event20(72, MeterEvent.POWERDOWN, "Power - Main power outage: start"),
    Event21(73, MeterEvent.POWERUP, "Power - Main power outage: end"),
    Event22(74, MeterEvent.BATTERY_VOLTAGE_LOW, "Power - Battery level below 10%: start"),
    Event23(85, MeterEvent.PROGRAM_FLOW_ERROR, "Software - Severe software error"),
    Event24(93, MeterEvent.BILLING_ACTION, "Billing period finished - async remote request"),
    Event25(96, MeterEvent.FIRMWARE_ACTIVATED, "New Firmware upgrade initiated"),
    Event26(115, MeterEvent.CONFIGURATIONCHANGE, "Disconnection of one or more physical module"),
    Event27(116, MeterEvent.TAMPER, "Magnetic field application appearance"),
    Event28(117, MeterEvent.TAMPER, "Magnetic field application disappearance"),
    Event29(118, MeterEvent.COVER_OPENED, "Access to electronic part"),
    Event30(119, MeterEvent.APPLICATION_ALERT_START, "Decryption error"),
    Event31(120, MeterEvent.APPLICATION_ALERT_START, "Authentication error"),
    Event32(121, MeterEvent.APPLICATION_ALERT_START, "Unauthorized access"),
    Event33(122, MeterEvent.CONFIGURATIONCHANGE, "Encryption keys programming"),
    Event34(123, MeterEvent.CONFIGURATIONCHANGE, "Encryption keys application"),
    Event35(124, MeterEvent.CONFIGURATIONCHANGE, "Unauthorized Battery Removal"),
    Event36(125, MeterEvent.CONFIGURATIONCHANGE, "Database Reset"),
    Event37(127, MeterEvent.APPLICATION_ALERT_START, "Database Corrupted"),

    EventUnknown(0, MeterEvent.APPLICATION_ALERT_START, "unkown");

    private int eventCode;
    private final int eisEventCode;
    private final String msg;

    private UnitsEvent(int eventCode, int eisEventCode, String msg)
    {
        this.eventCode = eventCode;
        this.eisEventCode = eisEventCode;
        this.msg = msg;
    }

    public int getUnitsEventCode()
    {
        return eventCode;
    }

    public int getEisEventCode()
    {
        return eisEventCode;
    }

    public String getMsg()
    {
        return msg;
    }

    public static UnitsEvent findEvent(int unitsEventCode)
    {
        for (UnitsEvent e : UnitsEvent.values())
        {
            if (e.getUnitsEventCode() == unitsEventCode)
            {
                return e;
            }
        }
        EventUnknown.eventCode = unitsEventCode;
        return EventUnknown;
    }

}
