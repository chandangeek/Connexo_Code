/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.events;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.energyict.mdc.protocol.api.cim.EndDeviceEventTypeMapping;

import java.util.*;

/**
 * Represents an event in a device.
 *
 * @author Karel
 */
public class MeterEvent implements java.io.Serializable, Comparable {

    private static final int UNKNOWN_ID = 0;
    private static final long serialVersionUID = -6814008454883084948L;

    /**
     * used if no generic code exists for this event
     */
    public static final int OTHER = 0;
    /**
     * powerdown event
     */
    public static final int POWERDOWN = 1;
    /**
     * powerup event
     */
    public static final int POWERUP = 2;
    /**
     * watchdog reset event
     */
    public static final int WATCHDOGRESET = 3;
    /**
     * Clock was changed. This event contains the time before the change
     */
    public static final int SETCLOCK_BEFORE = 4;
    /**
     * Clock was changed. This event contains the time after the change
     */
    public static final int SETCLOCK_AFTER = 5;
    /**
     * Clock was changed. The event time is either before or after the time after the change
     */
    public static final int SETCLOCK = 6;
    /**
     * The meter's configuration was changed
     */
    public static final int CONFIGURATIONCHANGE = 7;
    /**
     * An abnormal state in ram memory occured
     */
    public static final int RAM_MEMORY_ERROR = 8;
    /**
     * An abnormal flow in the code occured
     */
    public static final int PROGRAM_FLOW_ERROR = 9;
    /**
     * An overflow occured in one of the meter registers
     */
    public static final int REGISTER_OVERFLOW = 10;
    /**
     * An fatal error occured
     */
    public static final int FATAL_ERROR = 11;
    /**
     * All dala cleared
     */
    public static final int CLEAR_DATA = 12;
    /**
     * All dala cleared
     */
    public static final int HARDWARE_ERROR = 13;
    /**
     * All dala cleared
     */
    public static final int METER_ALARM = 14;  // e.g. Datawatt logger with serial application meter readout
    /**
     * An abnormal state in rom memory occured
     */
    public static final int ROM_MEMORY_ERROR = 15;
    /**
     * An event generated when the meter resets the maximum demand registers
     */
    public static final int MAXIMUM_DEMAND_RESET = 16;
    /**
     * An event generated when the meter performs a billing action
     */
    public static final int BILLING_ACTION = 17;
    /**
     * Start of a RTU Alarm
     */
    public static final int APPLICATION_ALERT_START = 18;
    /**
     * Stop of a RTU Alarm
     */
    public static final int APPLICATION_ALERT_STOP = 19;

    // Added by KV starting with the Transdata MarkV protocol 12/08/2005
    /**
     * Phase failure event
     */
    public static final int PHASE_FAILURE = 20;
    /**
     * Voltage sag event
     */
    public static final int VOLTAGE_SAG = 21;
    /**
     * Voltage swell event
     */
    public static final int VOLTAGE_SWELL = 22;

    // Added by GN for smart metering use cases
    /**
     * Tamper detection
     */
    public static final int TAMPER = 23;
    /**
     * The meter cover was opened
     */
    public static final int COVER_OPENED = 24;        // sometimes you have two events for the tamper detection
    /**
     * The terminal cover was opened
     */
    public static final int TERMINAL_OPENED = 25;

    public static final int REVERSE_RUN = 26;

    // Added by SvdB to support differentiation for smart metering
    public static final int LOADPROFILE_CLEARED = 27;

    // new events NTA
    /**
     * Indicates that the event log was cleared. This is always the
     * first entry in an event log. It is only stored in the affected
     * event log.
     */
    public static final int EVENT_LOG_CLEARED = 28;
    /**
     * Indicates the regular change from and to daylight saving
     * time. The time stamp shows the time before the change.
     * This event is not set in case of manual clock changes and
     * in case of power failures.
     */
    public static final int DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED = 29;
    /**
     * Indicates that clock may be invalid, i.e. if the power reserve
     * of the clock has exhausted. It is set at power up.
     */
    public static final int CLOCK_INVALID = 30;
    /**
     * Indicates that the battery must be exchanged due to the
     * expected end of life time.
     */
    public static final int REPLACE_BATTERY = 31;
    /**
     * Indicates that the current battery voltage is low.
     */
    public static final int BATTERY_VOLTAGE_LOW = 32;
    /**
     * Indicates that the passive TOU has been activated.
     */
    public static final int TOU_ACTIVATED = 33;
    /**
     * Indicates that the error register was cleared.
     */
    public static final int ERROR_REGISTER_CLEARED = 34;
    /**
     * Indicates that the alarm register was cleared.
     */
    public static final int ALARM_REGISTER_CLEARED = 35;
    /**
     * Indicates a physical or a logical error in the program memory.
     */
    public static final int PROGRAM_MEMORY_ERROR = 36;
    /**
     * Indicates a physical or a logical error in the non volatile memory
     */
    public static final int NV_MEMORY_ERROR = 37;
    /**
     * Indicates a watch dog microcontroller.
     */
    public static final int WATCHDOG_ERROR = 38;
    /**
     * Indicates a logical or physical error in the measurement system
     */
    public static final int MEASUREMENT_SYSTEM_ERROR = 39;
    /**
     * Indicates that the new firmware has been successfully
     * downloaded and verified, i.e. it is ready for activation
     */
    public static final int FIRMWARE_READY_FOR_ACTIVATION = 40;
    /**
     * Indicates that a new firmware has been activated
     */
    public static final int FIRMWARE_ACTIVATED = 41;
    /**
     * Indicates that the terminal cover has been closed
     */
    public static final int TERMINAL_COVER_CLOSED = 42;
    /**
     * Indicates that a strong magnetic DC field has been detected.
     */
    public static final int STRONG_DC_FIELD_DETECTED = 43;
    /**
     * Indicates that the strong magnetic DC field hasdisappeared.
     */
    public static final int NO_STRONG_DC_FIELD_ANYMORE = 44;
    /**
     * Indicates that the meter cover has been closed.
     */
    public static final int METER_COVER_CLOSED = 45;
    /**
     * Indicates that a user tried to gain access with a wrong password (intrusion detection)
     */
    public static final int N_TIMES_WRONG_PASSWORD = 46;
    /**
     * Indicates that the disconnector has been manually disconnected.
     */
    public static final int MANUAL_DISCONNECTION = 47;
    /**
     * Indicates that the disconnector has been manually connected.
     */
    public static final int MANUAL_CONNECTION = 48;
    /**
     * Indicates that the disconnector has been remotely disconnected.
     */
    public static final int REMOTE_DISCONNECTION = 49;
    /**
     * Indicates that the disconnector has been remotely connected.
     */
    public static final int REMOTE_CONNECTION = 50;
    /**
     * Indicates that the disconnector has been locally disconnected (i.e. via the limiter).
     */
    public static final int LOCAL_DISCONNECTION = 51;
    /**
     * Indicates that the limiter threshold has been exceeded.
     */
    public static final int LIMITER_THRESHOLD_EXCEEDED = 52;
    /**
     * Indicates that the monitored value of the limiter dropped below the threshold.
     */
    public static final int LIMITER_THRESHOLD_OK = 53;
    /**
     * Indicates that the limiter threshold has been changed
     */
    public static final int LIMITER_THRESHOLD_CHANGED = 54;
    /**
     * Indicates a communication problem when reading the meter connected
     */
    public static final int COMMUNICATION_ERROR_MBUS = 55;
    /**
     * Indicates that the communication with the M-Bus meter is ok again.
     */
    public static final int COMMUNICATION_OK_MBUS = 56;
    /**
     * Indicates that the battery must be exchanged due to the expected end of life time.
     */
    public static final int REPLACE_BATTERY_MBUS = 57;
    /**
     * Indicates that a fraud attempt has been registered.
     */
    public static final int FRAUD_ATTEMPT_MBUS = 58;
    /**
     * Indicates that the clock has been adjusted.
     */
    public static final int CLOCK_ADJUSTED_MBUS = 59;
    /**
     * Indicates that the disconnector has been manually disconnected.
     */
    public static final int MANUAL_DISCONNECTION_MBUS = 60;
    /**
     * Indicates that the disconnector has been manually connected.
     */
    public static final int MANUAL_CONNECTION_MBUS = 61;
    /**
     * Indicates that the disconnector has been remotely disconnected.
     */
    public static final int REMOTE_DISCONNECTION_MBUS = 62;
    /**
     * Indicates that the disconnector has been remotely connected.
     */
    public static final int REMOTE_CONNECTION_MBUS = 63;
    /**
     * Indicates that a valve alarm has been registered.
     */
    public static final int VALVE_ALARM_MBUS = 64;

    private final Date time;
    private final int eiCode;
    private final int protocolCode;
    private final String message;
    /**
     * Identifies the LogBook ID of the device (CIM logbook id)
     */
    private final int eventLogId;

    /**
     * Identifies the (sequential) ID of the event in the particular logbook.
     */
    private final int deviceEventId;
    /**
     * Keeps track of a list of additional information for this meterevent
     */
    private Map<String, String> additionalInfo = new HashMap<>();

    /**
     * String representation of this MeterEvent
     *
     * @return String
     */
    public String toString() {
        if (getMessage() != null) {
            return getMessage();
        }

        switch (eiCode) {
            case MeterEvent.POWERDOWN:
                return ("Power down.");
            case MeterEvent.POWERUP:
                return ("Power up.");
            case MeterEvent.CONFIGURATIONCHANGE:
                return ("Change in configuration.");
            case MeterEvent.REGISTER_OVERFLOW:
                return ("Register overflow.");
            case MeterEvent.PROGRAM_FLOW_ERROR:
                return ("Program flow error.");
            case MeterEvent.RAM_MEMORY_ERROR:
                return ("Ram memory error.");
            case MeterEvent.SETCLOCK:
                return ("Clock set.");
            case MeterEvent.SETCLOCK_AFTER:
                return ("Clock set after.");
            case MeterEvent.SETCLOCK_BEFORE:
                return ("Clock set before");
            case MeterEvent.WATCHDOGRESET:
                return ("Watchdog reset.");
            case MeterEvent.OTHER:
                return ("Other event.");
            case MeterEvent.FATAL_ERROR:
                return ("Fatal error.");
            case MeterEvent.CLEAR_DATA:
                return ("Clear data.");
            case MeterEvent.HARDWARE_ERROR:
                return ("Hardware error.");
            case MeterEvent.METER_ALARM:
                return ("Meter alarm.");
            case ROM_MEMORY_ERROR:
                return ("Rom memory error.");
            case MAXIMUM_DEMAND_RESET:
                return ("Maximum demand reset.");
            case BILLING_ACTION:
                return ("Billing action.");
            case PHASE_FAILURE:
                return ("Phase failure.");
            case APPLICATION_ALERT_START:
                return ("Application alert start.");
            case APPLICATION_ALERT_STOP:
                return ("Application alert stop.");
            case TAMPER:
                return ("Tamper detected.");
            case COVER_OPENED:
                return ("Meter cover opened.");
            case TERMINAL_OPENED:
                return ("Terminal cover opened.");
            case REVERSE_RUN:
                return ("Reverse Run.");
            case LOADPROFILE_CLEARED:
                return ("Load Profile cleared.");


            case EVENT_LOG_CLEARED:
                return ("Event log cleared");
            case DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED:
                return ("Daylight saving time enabled or disabled.");
            case CLOCK_INVALID:
                return ("Clock invalid.");
            case REPLACE_BATTERY:
                return ("Replace Battery.");
            case BATTERY_VOLTAGE_LOW:
                return ("Battery voltage low.");
            case TOU_ACTIVATED:
                return ("TOU activated.");
            case ERROR_REGISTER_CLEARED:
                return ("Error register cleared.");
            case ALARM_REGISTER_CLEARED:
                return ("Alarm register cleared.");
            case PROGRAM_MEMORY_ERROR:
                return ("Program memory error.");
            case NV_MEMORY_ERROR:
                return ("NV memory error.");
            case WATCHDOG_ERROR:
                return ("Watchdog error.");
            case MEASUREMENT_SYSTEM_ERROR:
                return ("Measurement system error.");
            case FIRMWARE_READY_FOR_ACTIVATION:
                return ("Firmware ready for activation.");
            case FIRMWARE_ACTIVATED:
                return ("Firmware activated.");
            case TERMINAL_COVER_CLOSED:
                return ("Terminal cover closed.");
            case STRONG_DC_FIELD_DETECTED:
                return ("Strong DC field detected.");
            case NO_STRONG_DC_FIELD_ANYMORE:
                return ("No strong DC field anymore.");
            case METER_COVER_CLOSED:
                return ("Meter cover closed.");
            case N_TIMES_WRONG_PASSWORD:
                return ("n times wrong password.");
            case MANUAL_DISCONNECTION:
                return ("Manual disconnection.");
            case MANUAL_CONNECTION:
                return ("Manual connection.");
            case REMOTE_DISCONNECTION:
                return ("Remote disconnection.");
            case REMOTE_CONNECTION:
                return ("Remote connection.");
            case LOCAL_DISCONNECTION:
                return ("Local disconnection.");
            case LIMITER_THRESHOLD_EXCEEDED:
                return ("Limiter threshold exceeded.");
            case LIMITER_THRESHOLD_OK:
                return ("Limiter threshold ok.");
            case LIMITER_THRESHOLD_CHANGED:
                return ("Limiter threshold changed.");
            case COMMUNICATION_ERROR_MBUS:
                return ("Communication error MBus.");
            case COMMUNICATION_OK_MBUS:
                return ("Communication ok M-Bus.");
            case REPLACE_BATTERY_MBUS:
                return ("Replace Battery M-Bus.");
            case FRAUD_ATTEMPT_MBUS:
                return ("Fraud attempt M-Bus.");
            case CLOCK_ADJUSTED_MBUS:
                return ("Clock adjusted M-Bus.");
            case MANUAL_DISCONNECTION_MBUS:
                return ("Manual disconnection M-Bus.");
            case MANUAL_CONNECTION_MBUS:
                return ("Manual connection M-Bus.");
            case REMOTE_DISCONNECTION_MBUS:
                return ("Remote disconnection MBus.");
            case REMOTE_CONNECTION_MBUS:
                return ("Remote connection MBus.");
            case VALVE_ALARM_MBUS:
                return ("Valve alarm M-Bus.");

            default:
                return ("Unknown event." + eiCode);

        } // switch(iLogCode)

    } // public String toString()


    /**
     * <p></p>
     *
     * @param time event time
     * @param eiCode generic event code
     */
    public MeterEvent(Date time, int eiCode) {
        this(time, eiCode, 0);
    }

    /**
     * <p></p>
     *
     * @param time event time
     * @param eiCode generic event code
     * @param protocolCode protocol specific event code
     */
    public MeterEvent(Date time, int eiCode, int protocolCode) {
        this(time, eiCode, protocolCode, null);
    }

    /**
     * <p></p>
     *
     * @param time event time
     * @param eiCode generic event code
     * @param message event message
     */
    public MeterEvent(Date time, int eiCode, String message) {
        this(time, eiCode, 0, message);
    }

    /**
     * <p></p>
     *
     * @param time event time
     * @param eiCode generic event code
     * @param protocolCode the protocol specific event code
     * @param message event message
     */
    public MeterEvent(Date time, int eiCode, int protocolCode, String message) {
        this(time, eiCode, protocolCode, message, 0, 0);
    }

    /**
     * @param time event time
     * @param eiCode generic event code
     * @param protocolCode the protocol specific event code
     * @param message event message
     * @param eventLogId device specific event Logbook Identification
     * @param deviceEventId device specific event ID
     */
    public MeterEvent(Date time, int eiCode, int protocolCode, String message, int eventLogId, int deviceEventId) {
        this.time = time;
        this.eiCode = eiCode;
        this.protocolCode = protocolCode;
        this.message = message;
        this.eventLogId = eventLogId;
        this.deviceEventId = deviceEventId;
    }

    public Date getTime() {
        return time;
    }

    public int getEiCode() {
        return eiCode;
    } // end getEiCode

    public int getProtocolCode() {
        return protocolCode;
    } // end getProtocolCode

    public String getMessage() {
        return message;
    } // end getMessage

    public int compareTo(Object o) {
        return (time.compareTo(((MeterEvent) o).getTime()));
    }

    public int getEventLogId() {
        return eventLogId;
    }

    public int getDeviceEventId() {
        return deviceEventId;
    }

    public static List<MeterProtocolEvent> mapMeterEventsToMeterProtocolEvents(List<MeterEvent> meterEvents, MeteringService meteringService) {
        List<MeterProtocolEvent> meterProtocolEvents = new ArrayList<>(meterEvents.size());
        for (MeterEvent event : meterEvents) {
            Optional<EndDeviceEventType> endDeviceEventType = EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(event.getEiCode(), meteringService);
            if (endDeviceEventType.isPresent()) {
                MeterProtocolEvent meterProtocolEvent = new MeterProtocolEvent(event.getTime(),
                        event.getEiCode(),
                        event.getProtocolCode(),
                        endDeviceEventType.get(),
                        event.getMessage(),
                        event.getEventLogId(),
                        event.getDeviceEventId());
                event.additionalInfo.entrySet().stream().forEach(keyValue -> meterProtocolEvent.addAdditionalInformation(keyValue.getKey(), keyValue.getValue()));
                meterProtocolEvents.add(meterProtocolEvent);
            }
        }
        return meterProtocolEvents;
    }

    public void addAdditionalInfo(String key, String value) {
        this.additionalInfo.put(key, value);
    }
}