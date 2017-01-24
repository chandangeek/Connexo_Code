package com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 14/12/11
 * Time: 11:41
 */
public class EventNumber {

    static List<EventNumber> events = new ArrayList<EventNumber>();

    private static final boolean EVENT_CAN_BE_IGNORED = true;

    static {
        events.add(new EventNumber(2, MeterEvent.CLEAR_DATA, "Indicates that tariff energy registers were cleared (but not the total energy registers)."));
        events.add(new EventNumber(3, MeterEvent.CLEAR_DATA, "Indicates that the load profile and/or the stored and/or stored value profile was cleared."));
        events.add(new EventNumber(4, MeterEvent.EVENT_LOG_CLEARED, "Indicates that the event log was cleared."));
        events.add(new EventNumber(5, MeterEvent.BATTERY_VOLTAGE_LOW, "Indicates that the battery voltage fell below a set threshold."));
        events.add(new EventNumber(7, MeterEvent.OTHER, "Indicates that the battery voltage returned to a level above a set threshold."));
        events.add(new EventNumber(8, MeterEvent.BILLING_ACTION, "Indicates that a billing period reset has occurred."));
        events.add(new EventNumber(9, MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED, "Indicates the change from and to daylight saving time. The time stamp shows the time before the disabled change."));
        events.add(new EventNumber(10, MeterEvent.SETCLOCK_BEFORE, "Indicates that the date/time has been adjusted. (old date/time) The time that is stored in the event log is the old time before adjusting the time."));
        events.add(new EventNumber(11, MeterEvent.SETCLOCK_AFTER, "Indicates that the date/time has been adjusted. (new date/time) The time that is stored in the event log is the new time after adjusting the time."));
        events.add(new EventNumber(12, MeterEvent.OTHER, "Indicates that the status of the input control signals have changed to OFF."));
        events.add(new EventNumber(13, MeterEvent.OTHER, "Indicates that the statue of the input control signals have changed to ON."));
        events.add(new EventNumber(14, MeterEvent.OTHER, "Start of interval", EVENT_CAN_BE_IGNORED));
        events.add(new EventNumber(15, MeterEvent.OTHER, "End of interval (regular internal)", EVENT_CAN_BE_IGNORED));
        events.add(new EventNumber(16, MeterEvent.OTHER, "End of interval (regular external)", EVENT_CAN_BE_IGNORED));
        events.add(new EventNumber(17, MeterEvent.VOLTAGE_SAG, "Indicates that an undervoltage on phase 1 occurred."));
        events.add(new EventNumber(18, MeterEvent.VOLTAGE_SAG, "Indicates that an undervoltage on phase 2 occurred."));
        events.add(new EventNumber(19, MeterEvent.VOLTAGE_SAG, "Indicates that an undervoltage on phase 3 occurred."));
        events.add(new EventNumber(20, MeterEvent.VOLTAGE_SWELL, "Indicates that an overvoltage on phase 1 occurred."));
        events.add(new EventNumber(21, MeterEvent.VOLTAGE_SWELL, "Indicates that an overvoltage on phase 2 occurred."));
        events.add(new EventNumber(22, MeterEvent.VOLTAGE_SWELL, "Indicates that an overvoltage on phase 3 occurred."));
        events.add(new EventNumber(23, MeterEvent.POWERDOWN, "Indicates that a power failure occurred."));
        events.add(new EventNumber(24, MeterEvent.POWERUP, "Indicates that the power had returned."));
        events.add(new EventNumber(25, MeterEvent.METER_ALARM, "Indicates that an overcurrent on phase 1 has occurred."));
        events.add(new EventNumber(26, MeterEvent.METER_ALARM, "Indicates that an overcurrent on phase 2 has occurred."));
        events.add(new EventNumber(27, MeterEvent.METER_ALARM, "Indicates that an overcurrent on phase 3 has occurred."));
        events.add(new EventNumber(28, MeterEvent.METER_ALARM, "Indicates that an overcurrent in the neutral neutral conductor has occurred."));
        events.add(new EventNumber(31, MeterEvent.METER_ALARM, "Indicates that the power factor 1 is below a set limit."));
        events.add(new EventNumber(32, MeterEvent.METER_ALARM, "Indicates that the power factor 2 is below a set limit."));
        events.add(new EventNumber(33, MeterEvent.METER_ALARM, "Indicates that demand 1 is above a set limit."));
        events.add(new EventNumber(34, MeterEvent.METER_ALARM, "Indicates that demand 2 is above a set limit."));
        events.add(new EventNumber(35, MeterEvent.METER_ALARM, "Indicates that demand 3 is above a set limit."));
        events.add(new EventNumber(36, MeterEvent.METER_ALARM, "Indicates that demand 4 is above a set limit."));
        events.add(new EventNumber(37, MeterEvent.METER_ALARM, "Indicates that demand 5 is above a set limit."));
        events.add(new EventNumber(38, MeterEvent.METER_ALARM, "Indicates that demand 6 is above a set limit."));
        events.add(new EventNumber(39, MeterEvent.METER_ALARM, "Indicates that demand 7 is above a set limit."));
        events.add(new EventNumber(40, MeterEvent.METER_ALARM, "Indicates that demand 8 is above a set limit."));
        events.add(new EventNumber(42, MeterEvent.OTHER, "EOI irregular by time changing."));
        events.add(new EventNumber(43, MeterEvent.OTHER, "EOI irregular by tariff switching."));
        events.add(new EventNumber(44, MeterEvent.OTHER, "The length of the capture period is incorrect. Normally that means the period is too short."));
        events.add(new EventNumber(45, MeterEvent.CLEAR_DATA, "Indicated that the error register was cleared."));
        events.add(new EventNumber(49, MeterEvent.PHASE_FAILURE, "Indicates that the voltage U1 dropped below 20 V."));
        events.add(new EventNumber(50, MeterEvent.PHASE_FAILURE, "Indicates that the voltage U2 dropped below 20 V."));
        events.add(new EventNumber(51, MeterEvent.PHASE_FAILURE, "Indicates that the voltage U3 dropped below 20 V."));
        events.add(new EventNumber(55, MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Indicates that the current L1 is above the minimum threshold of 0.1% Im while the voltage U1 is below 78% Un for more than to seconds."));
        events.add(new EventNumber(56, MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Indicates that the current L2 is above the minimum threshold of 0.1% Im while the voltage U2 is below 78% Un for more than to seconds."));
        events.add(new EventNumber(57, MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Indicates that the current L3 is above the minimum threshold of 0.1% Im while the voltage U3 is below 78% Un for more than to seconds."));
        events.add(new EventNumber(58, MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Indicates the additional power supply is missing."));
        events.add(new EventNumber(59, MeterEvent.CLEAR_DATA, "Indicates that all registers and profiles were cleared."));
        events.add(new EventNumber(63, MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Indicates the phase sequence is different from parameterised sequence."));
        events.add(new EventNumber(65, MeterEvent.BATTERY_VOLTAGE_LOW, "Indicates an empty or removed battery."));
        events.add(new EventNumber(66, MeterEvent.CLOCK_INVALID, "FF 02000000 Invalid clock."));
        events.add(new EventNumber(73, MeterEvent.FATAL_ERROR, "FF 00010000 Main memory (RAM)"));
        events.add(new EventNumber(74, MeterEvent.FATAL_ERROR, "FF 00020000 Backup memory access error."));
        events.add(new EventNumber(75, MeterEvent.FATAL_ERROR, "FF 00040000 Measuring system access error."));
        events.add(new EventNumber(76, MeterEvent.FATAL_ERROR, "FF 00080000 Time device access error."));
        events.add(new EventNumber(77, MeterEvent.FATAL_ERROR, "FF 00100000 Load profile memory access error."));
        events.add(new EventNumber(78, MeterEvent.FATAL_ERROR, "FF 00200000 RCR access error."));
        events.add(new EventNumber(79, MeterEvent.FATAL_ERROR, "FF 00400000 Communication unit access error."));
        events.add(new EventNumber(80, MeterEvent.FATAL_ERROR, "FF 00800000 Display board access error."));
        events.add(new EventNumber(81, MeterEvent.FATAL_ERROR, "FF 00000100 Program checksum error."));
        events.add(new EventNumber(82, MeterEvent.FATAL_ERROR, "FF 00000200 Backup data checksum error."));
        events.add(new EventNumber(83, MeterEvent.FATAL_ERROR, "FF 00000400 Parameter checksum error."));
        events.add(new EventNumber(84, MeterEvent.FATAL_ERROR, "FF 00000800 Load profile checksum error."));
        events.add(new EventNumber(85, MeterEvent.FATAL_ERROR, "FF 00001000 Stored values checksum error."));
        events.add(new EventNumber(86, MeterEvent.FATAL_ERROR, "FF 00002000 Event log checksum error."));
        events.add(new EventNumber(87, MeterEvent.FATAL_ERROR, "FF 00004000 Calibration data checksum error."));
        events.add(new EventNumber(88, MeterEvent.FATAL_ERROR, "FF 00008000 Load profile 2 checksum error."));
        events.add(new EventNumber(89, MeterEvent.PROGRAM_FLOW_ERROR, "FF 00000001 Invalid power down."));
        events.add(new EventNumber(90, MeterEvent.FATAL_ERROR, "FF 00000002 Measuring system error."));
        events.add(new EventNumber(93, MeterEvent.FATAL_ERROR, "FF 00000010 Expired watchdog."));
        events.add(new EventNumber(94, MeterEvent.FATAL_ERROR, "FF 00000020 Communication locked."));
        events.add(new EventNumber(95, MeterEvent.FATAL_ERROR, "FF 00000040 Wrong EEPROM/Flash. The meter will no longer work."));
        events.add(new EventNumber(96, MeterEvent.FATAL_ERROR, "FF 00000080 Wrong extension board identification."));
        events.add(new EventNumber(104, MeterEvent.CLEAR_DATA, "All general count registers were cleared."));
        events.add(new EventNumber(105, MeterEvent.FATAL_ERROR, "Indicates an SMS communication problem."));
        events.add(new EventNumber(106, MeterEvent.METER_ALARM, "Indicates that an alert has occurred."));
        events.add(new EventNumber(108, MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Indicates missing measurement voltage in al phases (complete outage)."));
        events.add(new EventNumber(111, MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Indicates that the apparent power exceeds a parameterised threshold."));
        events.add(new EventNumber(112, MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Indicates that the active power on L1 exceeds a parameterised threshold."));
        events.add(new EventNumber(113, MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Indicates that the active power on L2 exceeds a parameterised threshold."));
        events.add(new EventNumber(114, MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Indicates that the active power on L3 exceeds a parameterised threshold."));
        events.add(new EventNumber(115, MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Indicates that the reactive power on L1 exceeds a parameterised threshold."));
        events.add(new EventNumber(116, MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Indicates that the reactive power on L2 exceeds a parameterised threshold."));
        events.add(new EventNumber(117, MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Indicates that the reactive power on L3 exceeds a parameterised threshold."));
        events.add(new EventNumber(118, MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Indicates that the apparent power on L1 exceeds a parameterised threshold."));
        events.add(new EventNumber(119, MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Indicates that the apparent power on L2 exceeds a parameterised threshold."));
        events.add(new EventNumber(120, MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Indicates that the apparent power on L3 exceeds a parameterised threshold."));
        events.add(new EventNumber(124, MeterEvent.CONFIGURATIONCHANGE, "Indicates that either a transformer correction or a customer magnitude adjustment has been made."));
        events.add(new EventNumber(128, MeterEvent.CLEAR_DATA, "One or more total energy registers and/or energy registers have been cleared."));
        events.add(new EventNumber(133, MeterEvent.TERMINAL_OPENED, "Indicates that the terminal cover has been removed."));
        events.add(new EventNumber(134, MeterEvent.STRONG_DC_FIELD_DETECTED, "Indicates that a strong magnetic dc field has been detected."));
        events.add(new EventNumber(187, MeterEvent.TERMINAL_COVER_CLOSED, "Indicates that the terminal cover has been mounted."));
        events.add(new EventNumber(188, MeterEvent.STRONG_DC_FIELD_DETECTED, "Indicates that the dc field has been removed."));
        events.add(new EventNumber(193, MeterEvent.CLEAR_DATA, "Indicates that the load profile 2 was cleared."));
        events.add(new EventNumber(1024, MeterEvent.FATAL_ERROR, "Indicates that a fatal error occurred."));
        events.add(new EventNumber(524288, MeterEvent.FATAL_ERROR, "Indicates that a fatal error occurred."));
    }

    private final int protocolEventCode;
    private final int eisEventCode;
    private final String description;
    private final boolean ignore;

    private EventNumber(int protocolEventCode, int eisEventCode, String description) {
        this(protocolEventCode, eisEventCode, description, false);
    }

    private EventNumber(int protocolEventCode, int eisEventCode, String description, boolean ignore) {
        this.protocolEventCode = protocolEventCode;
        this.eisEventCode = eisEventCode;
        this.description = description;
        this.ignore = ignore;
    }

    static private EventNumber getEventNumber(int protocolEventCode) {
        for (EventNumber eventNumber : events) {
            if (eventNumber.getProtocolEventCode() == protocolEventCode) {
                return eventNumber;
            }
        }
        return null;
    }

    static public MeterEvent toMeterEvent(int protocolEventCode, Date dateTime) {
        EventNumber eventNumber = EventNumber.getEventNumber(protocolEventCode);
        if (eventNumber == null) {
            return new MeterEvent(dateTime, MeterEvent.OTHER, "Unknown event code " + eventNumber);
        } else if (eventNumber.isIgnore()) {
            return null;
        }
        return new MeterEvent(dateTime, eventNumber.getEisEventCode(), protocolEventCode, eventNumber.getDescription());
    }

    private int getProtocolEventCode() {
        return protocolEventCode;
    }

    public int getEisEventCode() {
        return eisEventCode;
    }

    private String getDescription() {
        return description;
    }

    public boolean isIgnore() {
        return ignore;
    }
}