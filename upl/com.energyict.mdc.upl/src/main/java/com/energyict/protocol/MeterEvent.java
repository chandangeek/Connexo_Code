/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocol;

import com.energyict.cim.EndDeviceEventTypeMapping;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents an event in a device.
 *
 * @author Karel
 */
public class MeterEvent implements Serializable, Comparable<MeterEvent> {

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

    public static final int MAX_NUMBER_OF_GENERIC_EVENTS = 64;

    /**
     * Start of Beacon3100 custom codes
     * Range from 100000 to 100073
     * Actual codes MUST be remapped and CIM code corespondents created.
     * Event description is currently used in Beacon3100 protocol class.
     * 02.02.2018
     * In case event codes remain the same table EISRTUEVENTS must be modified to support 6 digit numbers
     * ALTER TABLE EISRTUEVENT
     * MODIFY (CODE NUMBER(6,0));
     */

    /**
     *  CLEARED
     */
    public static final int CLEARED = 100001;

    /**
     *  Power management switch low power
     */
    public static final int POWER_MANAGEMENT_SWITCH_LOW_POWER = 100002;

    /**
     *   Power management switch full power
     */
    public static final int POWER_MANAGEMENT_SWITCH_FULL_POWER = 100003;

    /**
     *  Power management switch reduced power
     */
    public static final int POWER_MANAGEMENT_SWITCH_REDUCED_POWER = 100004;

    /**
     *  Power management mains lost
     */
    public static final int POWER_MANAGEMENT_MAINS_LOST = 100005;

    /**
     *  Power management mains recovered
     */
    public static final int POWER_MANAGEMENT_MAINS_RECOVERED = 100006;

    /**
     *  Power management last gasp
     */
    public static final int POWER_MANAGEMENT_LAST_GASP = 100007;

    /**
     *  Power management battery charge start
     */
    public static final int POWER_MANAGEMENT_BATTERY_CHARGE_START = 100008;

    /**
     *  Power management battery charge stop
     */
    public static final int POWER_MANAGEMENT_BATTERY_CHARGE_STOP = 100009;

    /**
     *  Idis meter discovery
     */
    public static final int IDIS_METER_DISCOVERY = 1000010;
    /**
     *  Idis meter accepted
     */
    public static final int IDIS_METER_ACCEPTED = 1000011;

    /**
     *  Idis meter rejected
     */
    public static final int IDIS_METER_REJECTED = 100012;

    /**
     *  Idis meter alarm
     */
    public static final int IDIS_METER_ALARM = 100013;

    /**
     *  Idis alarm condition
     */
    public static final int IDIS_ALARM_CONDITION = 100014;

    /**
     *  Idis multi master
     */
    public static final int IDIS_MULTI_MASTER = 100015;

    /**
     *  Idis plc equipment in state new
     */
    public static final int IDIS_PLC_EQUIPMENT_IN_STATE_NEW = 100016;

    /**
     *  Idis extended alarm status
     */
    public static final int IDIS_EXTENDED_ALARM_STATUS = 100017;

    /**
     *  Idis meter deleted
     */
    public static final int IDIS_METER_DELETED = 100018;

    /**
     *  Idis stack event
     */
    public static final int IDIS_STACK_EVENT = 100019;

    /**
     *  Plc prime restarted
     */
    public static final int PLC_PRIME_RESTARTED = 100020;

    /**
     *  Plc prime stack event
     */
    public static final int PLC_PRIME_STACK_EVENT = 100021;

    /**
     *  Plc prime register node
     */
    public static final int PLC_PRIME_REGISTER_NODE = 100022;

    /**
     *  Plc prime unregister node
     */
    public static final int PLC_PRIME_UNREGISTER_NODE = 100023;

    /**
     *  Plc g3 restarted
     */
    public static final int PLC_G3_RESTARTED = 100024;

    /**
     *  Plc g3 stack event
     */
    public static final int PLC_G3_STACK_EVENT = 100025;

    /**
     *  Plc g3 register node
     */
    public static final int PLC_G3_REGISTER_NODE = 100026;

    /**
     * Plc g3 unregister node
     */
    public static final int PLC_G3_UNREGISTER_NODE = 100027;

    /**
     *  Plc g3 event received
     */
    public static final int PLC_G3_EVENT_RECEIVED = 100028;

    /**
     *  Plc g3 join request node
     */
    public static final int PLC_G3_JOIN_REQUEST_NODE = 100029;

    /**
     *  Plc g3 uppermac stopped
     */
    public static final int PLC_G3_UPPERMAC_STOPPED = 100030;

    /**
     *  plc g3 uppermac started
     */
    public static final int PLC_G3_UPPERMAC_STARTED = 100031;

    /**
     *  Plc g3 join failed
     */
    public static final int PLC_G3_JOIN_FAILED = 100032;

    /**
     *  Plc g3 auth failure
     */
    public static final int PLC_G3_AUTH_FAILURE = 100033;

    /**
     *  Dlms server session accepted
     */
    public static final int DLMS_SERVER_SESSION_ACCEPTED = 100034;

    /**
     *  Dlms server session finished
     */
    public static final int DLMS_SERVER_SESSION_FINISHED = 100035;

    /**
     * Dlms other
     */
    public static final int DLMS_OTHER = 100036;

    /**
     *  Dlms upstream test
     */
    public static final int DLMS_UPSTREAM_TEST = 100037;

    /**
     *  Modem wdg pppd reset
     */
    public static final int MODEM_WDG_PPPD_RESET = 100038;

    /**
     *  Modem wdg hw reset
     */
    public static final int MODEM_WDG_HW_RESET = 100039;

    /**
     *  Modem wdg reboot requested
     */
    public static final int MODEM_WDG_REBOOT_REQUESTED = 100040;

    /**
     *  Modem connected
     */
    public static final int MODEM_CONNECTED = 100041;

    /**
     *  Modem disconnected
     */
    public static final int MODEM_DISCONNECTED = 100042;

    /**
     *  Modem wake up
     */
    public static final int MODEM_WAKE_UP = 100043;

    /**
     *  Protocol preliminary task completed
     */
    public static final int PROTOCOL_PRELIMINARY_TASK_COMPLETED = 100044;

    /**
     *  Protocol preliminary task failed
     */
    public static final int PROTOCOL_PRELIMINARY_TASK_FAILED = 100045;

    /**
     *  Protocol consecutive failure
     */
    public static final int PROTOCOL_CONSECUTIVE_FAILURE = 100046;

    /**
     *  Firmware upgrade
     */
    public static final int FIRMWARE_UPGRADE = 100047;

    /**
     *  Firmware modified
     */
    public static final int FIRMWARE_MODIFIED = 100048;

    /**
     *  CPU overload
     */
    public static final int CPU_OVERLOAD = 100049;

    /**
     *  RAM too high
     */
    public static final int RAM_TOO_HIGH = 100050;

    /**
     *  Disk usage too high
     */
    public static final int DISK_USAGE_TOO_HIGH = 100051;

    /**
     *  Pace exception
     */
    public static final int PACE_EXCEPTION = 100052;

    /**
     *  SSH login
     */
    public static final int SSH_LOGIN = 100053;

    /**
     *  Factory reset
     */
    public static final int FACTORY_RESET = 100054;

    /**
     *  Webportal login
     */
    public static final int WEBPORTAL_LOGIN = 100055;

    /**
     *  Webportal action
     */
    public static final int WEBPORTAL_ACTION = 100056;

    /**
     *  Webportal failed login
     */
    public static final int WEBPORTAL_FAILED_LOGIN = 100057;

    /**
     *  Webportal locked user
     */
    public static final int WEBPORTAL_LOCKED_USER = 100058;

    /**
     *  Meter multicast upgrade start
     */
    public static final int METER_MULTICAST_UPGRADE_START = 100059;

    /**
     *  Meter multicast upgrade completed
     */
    public static final int METER_MULTICAST_UPGRADE_COMPLETED = 100060;

    /**
     *  Meter multicast upgrade failed
     */
    public static final int METER_MULTICAST_UPGRADE_FAILED = 100061;

    /**
     *  Meter multicast upgrade info
     */
    public static final int METER_MULTICAST_UPGRADE_INFO = 100062;

    /**
     *  General security error
     */
    public static final int GENERAL_SECURITY_ERROR = 100063;

    /**
     *  Wrap key error
     */
    public static final int WRAP_KEY_ERROR = 100064;

    /**
     *  Dlms authentication level updated
     */
    public static final int DLMS_AUTHENTICATION_LEVEL_UPDATED = 100065;

    /**
     *  Dlms security policy updated
     */
    public static final int DLMS_SECURITY_POLICY_UPDATED = 100066;

    /**
     *  Dlms security suite updated
     */
    public static final int DLMS_SECURITY_SUITE_UPDATED = 100067;

    /**
     *  Dlms keys updated
     */
    public static final int DLMS_KEYS_UPDATED = 100069;

    /**
     *  Dlms access violation
     */
    public static final int DLMS_ACCESS_VIOLATION = 100070;

    /**
     *  Dlms authentication failure
     */
    public static final int DLMS_AUTHENTICATION_FAILURE = 100071;

    /**
     *  Dlms ciphering error
     */
    public static final int DLMS_CIPHERING_ERROR = 100072;

    /**
     * Unknown register
     */
    public static final int UNKNOWN_REGISTER = 100073;


    // Used by EIServer UI:
    // !!! Keep this one in sync with the above !!!
    // total number of events - 1 (first event starts at 0)
    //Left at 64, custom Beacon events must be remapped
    public static final int MAX_NUMBER_OF_EVENTS = 64;

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
     * Keeps track of additional information for this meterevent.
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
        return codeToMessage(eiCode);
    }

    public static String codeToMessage(int code){
        switch (code) {
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
            case POWER_MANAGEMENT_SWITCH_LOW_POWER:
                return ("Power management switch low power.");
            case POWER_MANAGEMENT_SWITCH_FULL_POWER:
                return ("Power management switch full power.");
            case POWER_MANAGEMENT_SWITCH_REDUCED_POWER:
                return ("Power management switch reduced power");
            case POWER_MANAGEMENT_MAINS_LOST:
                return ("power management mains lost.");
            case POWER_MANAGEMENT_MAINS_RECOVERED:
                return ("Power management mains recovered.");
            case POWER_MANAGEMENT_LAST_GASP:
                return ("Power management last gasp.");
            case POWER_MANAGEMENT_BATTERY_CHARGE_START:
                return ("Power management battery charge start.");
            case POWER_MANAGEMENT_BATTERY_CHARGE_STOP:
                return ("Power management battery charge stop.");
            case IDIS_METER_DISCOVERY:
                return ("IDIS meter discovery.");
            case IDIS_METER_ACCEPTED:
                return ("IDIS meter accepted.");
            case IDIS_METER_REJECTED:
                return ("IDIS meter rejected.");
            case IDIS_METER_ALARM:
                return ("IDIS meter alarm.");
            case IDIS_ALARM_CONDITION:
                return ("IDIS alarm condition.");
            case IDIS_MULTI_MASTER:
                return ("IDIS multi master");
            case IDIS_PLC_EQUIPMENT_IN_STATE_NEW:
                return ("IDIS PLC equipment in state new.");
            case IDIS_EXTENDED_ALARM_STATUS:
                return ("IDIS extended alarm status.");
            case IDIS_METER_DELETED:
                return ("IDIS meter deleted.");
            case IDIS_STACK_EVENT:
                return ("IDIS stack event.");
            case PLC_PRIME_RESTARTED:
                return ("PLC prime restarted.");
            case PLC_PRIME_STACK_EVENT:
                return ("PLC prime stack event.");
            case PLC_PRIME_REGISTER_NODE:
                return ("PLC prime register node.");
            case PLC_PRIME_UNREGISTER_NODE:
                return ("PLC prime unregister node.");
            case PLC_G3_RESTARTED:
                return ("PLC G3 restarted.");
            case PLC_G3_STACK_EVENT:
                return ("PLC G3 stack event.");
            case PLC_G3_REGISTER_NODE:
                return ("PLC G3 register node.");
            case PLC_G3_UNREGISTER_NODE:
                return ("PLC G3 unregister node.");
            case PLC_G3_EVENT_RECEIVED:
                return ("PLC G3 event received.");
            case PLC_G3_JOIN_REQUEST_NODE:
                return ("PLC G3 join request node.");
            case PLC_G3_UPPERMAC_STOPPED:
                return ("PLC G3 uppermac stopped.");
            case PLC_G3_UPPERMAC_STARTED:
                return ("PLC G3 uppermac started.");
            case PLC_G3_JOIN_FAILED:
                return ("PLC G3 join failed.");
            case PLC_G3_AUTH_FAILURE:
                return ("PLC G3 auth failure.");
            case DLMS_SERVER_SESSION_ACCEPTED:
                return ("Dlms server session accepted.");
            case DLMS_SERVER_SESSION_FINISHED:
                return ("Dlms server session finished.");
            case DLMS_OTHER:
                return ("Dlms other.");
            case DLMS_UPSTREAM_TEST:
                return ("Dlms upstream test.");
            case MODEM_WDG_PPPD_RESET:
                return ("Modem wdg pppd reset.");
            case MODEM_WDG_HW_RESET:
                return ("Modem wdg hw reset.");
            case MODEM_WDG_REBOOT_REQUESTED:
                return ("Modem wdg reboot requested.");
            case MODEM_CONNECTED:
                return ("Modem connected.");
            case MODEM_DISCONNECTED:
                return ("Modem disconnected.");
            case MODEM_WAKE_UP:
                return ("Modem wake up.");
            case PROTOCOL_PRELIMINARY_TASK_COMPLETED:
                return ("Protocol preliminary task completed.");
            case PROTOCOL_PRELIMINARY_TASK_FAILED:
                return ("Protocol preliminary task failed.");
            case PROTOCOL_CONSECUTIVE_FAILURE:
                return ("Protocol consecutive failure.");
            case FIRMWARE_UPGRADE:
                return ("Firmware upgrade.");
            case FIRMWARE_MODIFIED:
                return ("Firmware modified");
            case CPU_OVERLOAD:
                return ("CPU overload.");
            case RAM_TOO_HIGH:
                return ("RAM too high.");
            case DISK_USAGE_TOO_HIGH:
                return ("Disk usage too high.");
            case PACE_EXCEPTION:
                return ("Pace exception.");
            case SSH_LOGIN:
                return ("SSH login.");
            case FACTORY_RESET:
                return ("Factory reset.");
            case WEBPORTAL_LOGIN:
                return ("Webportal login.");
            case WEBPORTAL_ACTION:
                return ("Webportal action.");
            case WEBPORTAL_FAILED_LOGIN:
                return ("Webportal failed login.");
            case WEBPORTAL_LOCKED_USER:
                return ("Webportal locked user.");
            case METER_MULTICAST_UPGRADE_START:
                return ("Meter multicast upgrade start.");
            case METER_MULTICAST_UPGRADE_COMPLETED:
                return ("Meter multicast upgrade completed.");
            case METER_MULTICAST_UPGRADE_FAILED:
                return ("Meter multicast upgrade failed.");
            case METER_MULTICAST_UPGRADE_INFO:
                return ("Meter multicast upgrade info.");
            case GENERAL_SECURITY_ERROR:
                return ("General security error.");
            case WRAP_KEY_ERROR:
                return ("Wrap key error.");
            case DLMS_AUTHENTICATION_LEVEL_UPDATED:
                return ("Dlms authentication level updated.");
            case DLMS_SECURITY_POLICY_UPDATED:
                return ("Dlms security policy updated.");
            case DLMS_SECURITY_SUITE_UPDATED:
                return ("Dlms security suite updated.");
            case DLMS_KEYS_UPDATED:
                return ("Dlms keys updated.");
            case DLMS_ACCESS_VIOLATION:
                return ("Dlms access violation.");
            case DLMS_AUTHENTICATION_FAILURE:
                return ("Dlms authentication failure.");
            case DLMS_CIPHERING_ERROR:
                return ("Dlms ciphering error.");
            case CLEARED:
                return ("Cleared.");
            case UNKNOWN_REGISTER:
                return ("Unknown register.");
            default:
                return ("Unknown event." + code);

        } // switch(iLogCode)
    }

    /**
     * <p></p>
     *
     * @param time   event time
     * @param eiCode generic event code
     */
    public MeterEvent(Date time, int eiCode) {
        this(time, eiCode, 0);
    }

    /**
     * <p></p>
     *
     * @param time         event time
     * @param eiCode       generic event code
     * @param protocolCode protocol specific event code
     */
    public MeterEvent(Date time, int eiCode, int protocolCode) {
        this(time, eiCode, protocolCode, null);
    }

    /**
     * <p></p>
     *
     * @param time    event time
     * @param eiCode  generic event code
     * @param message event message
     */
    public MeterEvent(Date time, int eiCode, String message) {
        this(time, eiCode, 0, message);
    }

    /**
     * <p></p>
     *
     * @param time         event time
     * @param eiCode       generic event code
     * @param protocolCode the protocol specific event code
     * @param message      event message
     */
    public MeterEvent(Date time, int eiCode, int protocolCode, String message) {
        this(time, eiCode, protocolCode, message, 0, 0);
    }

    /**
     * @param time          event time
     * @param eiCode        generic event code
     * @param protocolCode  the protocol specific event code
     * @param message       event message
     * @param eventLogId    device specific event Logbook Identification
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


    /**
     * <p></p>
     *
     * @return the event time
     */
    public Date getTime() {
        return time;
    } // end getTime

    /**
     * <p></p>
     *
     * @return the generic event code
     */
    public int getEiCode() {
        return eiCode;
    } // end getEiCode

    /**
     * <p></p>
     *
     * @return the protocol specific event code
     */
    public int getProtocolCode() {
        return protocolCode;
    } // end getProtocolCode

    /**
     * <p></p>
     *
     * @return the event's message
     */
    public String getMessage() {
        return message;
    } // end getMessage


    /**
     * @return the {@link #eventLogId}
     */
    public int getEventLogId() {
        return eventLogId;
    }

    /**
     * @return the {@link #deviceEventId}
     */
    public int getDeviceEventId() {
        return deviceEventId;
    }

    public void addAdditionalInfo(String key, String value) {
        this.additionalInfo.put(key, value);
    }

    @Override
    public int compareTo(MeterEvent other) {
        return (time.compareTo(other.getTime()));
    }

    public static MeterProtocolEvent mapMeterEventToMeterProtocolEvent(MeterEvent event) {
        return new MeterProtocolEvent(event.getTime(),
                event.getEiCode(),
                event.getProtocolCode(),
                EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(event.getEiCode()),
                event.getMessage(),
                UNKNOWN_ID,
                UNKNOWN_ID);
    }

    public static List<MeterProtocolEvent> mapMeterEventsToMeterProtocolEvents(List<MeterEvent> meterEvents) {
        return meterEvents.stream().map(MeterEvent::toMeterProtocolEvent).collect(Collectors.toList());
    }

    private static MeterProtocolEvent toMeterProtocolEvent(MeterEvent event) {
        MeterProtocolEvent meterProtocolEvent =
                new MeterProtocolEvent(
                        event.getTime(),
                        event.getEiCode(),
                        event.getProtocolCode(),
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(event.getEiCode()),
                        event.getMessage(),
                        UNKNOWN_ID,
                        UNKNOWN_ID);
        event.additionalInfo
                .entrySet()
                .stream()
                .forEach(keyValue -> meterProtocolEvent.addAdditionalInformation(keyValue.getKey(), keyValue.getValue()));
        return meterProtocolEvent;
    }

}