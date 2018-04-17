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
     * Range from 100000 to 100129
     * Actual codes MUST be remapped and CIM code corespondents created.
     * Event description is currently used in Beacon3100 protocol class.
     * 02.02.2018
     * In case event codes remain the same table EISRTUEVENTS must be modified to support 6 digit numbers
     * ALTER TABLE EISRTUEVENT
     * MODIFY (CODE NUMBER(6,0));
     */

    public static final int CLEARED = 100001;

    public static final int POWER_MANAGEMENT_SWITCH_LOW_POWER = 100002;

    public static final int POWER_MANAGEMENT_SWITCH_FULL_POWER = 100003;

    public static final int POWER_MANAGEMENT_SWITCH_REDUCED_POWER = 100004;

    public static final int POWER_MANAGEMENT_MAINS_LOST = 100005;

    public static final int POWER_MANAGEMENT_MAINS_RECOVERED = 100006;

    public static final int POWER_MANAGEMENT_LAST_GASP = 100007;

    public static final int POWER_MANAGEMENT_BATTERY_CHARGE_START = 100008;

    public static final int POWER_MANAGEMENT_BATTERY_CHARGE_STOP = 100009;

    public static final int IDIS_METER_DISCOVERY = 100010;

    public static final int IDIS_METER_ACCEPTED = 100011;

    public static final int IDIS_METER_REJECTED = 100012;

    public static final int IDIS_METER_ALARM = 100013;

    public static final int IDIS_ALARM_CONDITION = 100014;

    public static final int IDIS_MULTI_MASTER = 100015;

    public static final int IDIS_PLC_EQUIPMENT_IN_STATE_NEW = 100016;

    public static final int IDIS_EXTENDED_ALARM_STATUS = 100017;

    public static final int IDIS_METER_DELETED = 100018;

    public static final int IDIS_STACK_EVENT = 100019;

    public static final int PLC_PRIME_RESTARTED = 100020;

    public static final int PLC_PRIME_STACK_EVENT = 100021;

    public static final int PLC_PRIME_REGISTER_NODE = 100022;

    public static final int PLC_PRIME_UNREGISTER_NODE = 100023;

    public static final int PLC_G3_RESTARTED = 100024;

    public static final int PLC_G3_STACK_EVENT = 100025;

    public static final int PLC_G3_REGISTER_NODE = 100026;

    public static final int PLC_G3_UNREGISTER_NODE = 100027;

    public static final int PLC_G3_EVENT_RECEIVED = 100028;

    public static final int PLC_G3_JOIN_REQUEST_NODE = 100029;

    public static final int PLC_G3_UPPERMAC_STOPPED = 100030;

    public static final int PLC_G3_UPPERMAC_STARTED = 100031;

    public static final int PLC_G3_JOIN_FAILED = 100032;

    public static final int PLC_G3_AUTH_FAILURE = 100033;

    public static final int DLMS_SERVER_SESSION_ACCEPTED = 100034;

    public static final int DLMS_SERVER_SESSION_FINISHED = 100035;

    public static final int DLMS_OTHER = 100036;

    public static final int DLMS_UPSTREAM_TEST = 100037;

    public static final int MODEM_WDG_PPPD_RESET = 100038;

    public static final int MODEM_WDG_HW_RESET = 100039;

    public static final int MODEM_WDG_REBOOT_REQUESTED = 100040;

    public static final int MODEM_CONNECTED = 100041;

    public static final int MODEM_DISCONNECTED = 100042;

    public static final int MODEM_WAKE_UP = 100043;

    public static final int PROTOCOL_PRELIMINARY_TASK_COMPLETED = 100044;

    public static final int PROTOCOL_PRELIMINARY_TASK_FAILED = 100045;

    public static final int PROTOCOL_CONSECUTIVE_FAILURE = 100046;

    public static final int FIRMWARE_UPGRADE = 100047;

    public static final int FIRMWARE_MODIFIED = 100048;

    public static final int CPU_OVERLOAD = 100049;

    public static final int RAM_TOO_HIGH = 100050;

    public static final int DISK_USAGE_TOO_HIGH = 100051;

    public static final int PACE_EXCEPTION = 100052;

    public static final int SSH_LOGIN = 100053;

    public static final int FACTORY_RESET = 100054;

    public static final int WEBPORTAL_LOGIN = 100055;

    public static final int WEBPORTAL_ACTION = 100056;

    public static final int WEBPORTAL_FAILED_LOGIN = 100057;

    public static final int WEBPORTAL_LOCKED_USER = 100058;

    public static final int METER_MULTICAST_UPGRADE_START = 100059;

    public static final int METER_MULTICAST_UPGRADE_COMPLETED = 100060;

    public static final int METER_MULTICAST_UPGRADE_FAILED = 100061;

    public static final int METER_MULTICAST_UPGRADE_INFO = 100062;

    public static final int GENERAL_SECURITY_ERROR = 100063;

    public static final int WRAP_KEY_ERROR = 100064;

    public static final int DLMS_AUTHENTICATION_LEVEL_UPDATED = 100065;

    public static final int DLMS_SECURITY_POLICY_UPDATED = 100066;

    public static final int DLMS_SECURITY_SUITE_UPDATED = 100067;

    public static final int DLMS_KEYS_UPDATED = 100069;

    public static final int DLMS_ACCESS_VIOLATION = 100070;

    public static final int DLMS_AUTHENTICATION_FAILURE = 100071;

    public static final int DLMS_CIPHERING_ERROR = 100072;

    public static final int UNKNOWN_REGISTER = 100073;

    public static final int PLC_G3_BLACKLIST = 100074;

    public static final int PLC_G3_NODE_LINK_LOST = 100075;

    public static final int PLC_G3_NODE_LINK_RECOVERED = 100076;

    public static final int PLC_G3_PAN_ID = 100077;

    public static final int PLC_G3_TOPOLOGY_UPDATE = 100078;

    public static final int MODEM_NEW_SIM = 100079;

    public static final int MODEM_NEW_EQUIPMENT = 100080;

    public static final int CHECK_DATA_CONCENTRATOR_CONFIG = 100081;

    public static final int LINK_UP = 100082;

    public static final int LINK_DOWN = 100083;

    public static final int USB_ADD = 100084;

    public static final int USB_REMOVE = 100085;

    public static final int FILE_TRANSFER_COMPLETED = 100086;

    public static final int FILE_TRANSFER_FAILED = 100087;

    public static final int SCRIPT_EXECUTION_STARTED = 100088;

    public static final int SCRIPT_EXECUTION_COMPLETED = 100089;

    public static final int SCRIPT_EXECUTION_FAILED = 100090;

    public static final int SCRIPT_EXECUTION_SCHEDULED = 100091;

    public static final int SCRIPT_EXECUTION_DESCHEDULED = 100092;

    public static final int WEBPORTAL_CSRF_ATTACK = 100093;

    public static final int SNMP_OTHER = 100094;

    public static final int SNMP_INFO = 100095;

    public static final int SNMP_UNSUPPORTED_VERSION = 100096;

    public static final int SNMP_UNSUPPORTED_SEC_MODEL = 100097;

    public static final int SNMP_INVALID_USER_NAME = 100098;

    public static final int SNMP_INVALID_ENGINE_ID = 100099;

    public static final int SNMP_AUTHENTICATION_FAILURE = 100100;

    public static final int SNMP_KEYS_UPDATED = 100101;

    public static final int CRL_UPDATED = 100102;

    public static final int CRL_UPDATE_REJECTED = 100103;

    public static final int KEY_UPDATE_REQUEST = 100104;

    public static final int CRL_REMOVED = 100105;

    public static final int DOT1X_SUCCESS = 100106;

    public static final int DOT1X_FAILURE = 100107;

    public static final int REPLAY_ATTACK = 100108;

    public static final int CERTIFICATE_ADDED = 100109;

    public static final int CERTIFICATE_REMOVED = 100110;

    public static final int CERTIFICATE_EXPIRED = 100111;

    // GENERAL
    public static final int POWER_DOWN_POWER_LOST              = 100112;
    public static final int POWER_DOWN_USER_REQUEST            = 100113;
    public static final int POWER_DOWN_SOFTWARE_FAULT          = 100114;
    public static final int POWER_DOWN_HARDWARE_FAULT          = 100115;
    public static final int POWER_DOWN_NETWORK_INACTIVITY      = 100116;
    public static final int POWER_DOWN_FIRMWARE_UPGRADE        = 100117;
    public static final int POWER_DOWN_FIRMWARE_ROLLBACK       = 100118;
    public static final int POWER_DOWN_DISK_ERROR              = 100119;
    public static final int POWER_DOWN_CONFIGURATION_ERROR     = 100120;
    public static final int POWER_DOWN_FACTORY_RESET           = 100121;
    public static final int POWER_DOWN_TAMPERING               = 100122;
    public static final int POWER_DOWN_TEMPERATURE             = 100123;
    public static final int POWER_DOWN_SYSTEM_WATCHDOG         = 100124;
    public static final int POWER_DOWN_WWAN_MODEM_WATCHDOG     = 100125;
    public static final int POWER_DOWN_SECURE_ELEMENT_WATCHDOG = 100126;
    public static final int POWER_DOWN_EXTERNAL_WATCHDOG       = 100127;
    public static final int PROTOCOL_LOG_CLEARED               = 100128;
    public static final int METER_CLOCK_INVALID                = 100129;


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
            case POWERDOWN:
                return ("Power down.");
            case POWERUP:
                return ("Power up.");
            case CONFIGURATIONCHANGE:
                return ("Change in configuration.");
            case REGISTER_OVERFLOW:
                return ("Register overflow.");
            case PROGRAM_FLOW_ERROR:
                return ("Program flow error.");
            case RAM_MEMORY_ERROR:
                return ("Ram memory error.");
            case SETCLOCK:
                return ("Clock set.");
            case SETCLOCK_AFTER:
                return ("Clock set after.");
            case SETCLOCK_BEFORE:
                return ("Clock set before");
            case WATCHDOGRESET:
                return ("Watchdog reset.");
            case OTHER:
                return ("Other event.");
            case FATAL_ERROR:
                return ("Fatal error.");
            case CLEAR_DATA:
                return ("Clear data.");
            case HARDWARE_ERROR:
                return ("Hardware error.");
            case METER_ALARM:
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
            case PLC_G3_BLACKLIST:
                return "PLC G3 blacklist.";
            case PLC_G3_NODE_LINK_LOST:
                return "PLC G3 node link lost.";
            case PLC_G3_NODE_LINK_RECOVERED:
                return "PLC G3 node link recovered.";
            case PLC_G3_PAN_ID:
                return "PLC G3 PAN ID.";
            case PLC_G3_TOPOLOGY_UPDATE:
                return "PLC G3 topology update.";
            case MODEM_NEW_SIM:
                return "Modem new SIM.";
            case MODEM_NEW_EQUIPMENT:
                return "Modem new equipment.";
            case CHECK_DATA_CONCENTRATOR_CONFIG:
                return "Check data concentrator config.";
            case LINK_UP:
                return "Link up.";
            case LINK_DOWN:
                return "Link down.";
            case USB_ADD:
                return "USB add.";
            case USB_REMOVE:
                return "USB remove.";
            case FILE_TRANSFER_COMPLETED:
                return "File transfer completed.";
            case FILE_TRANSFER_FAILED:
                return "File transfer failed.";
            case SCRIPT_EXECUTION_STARTED:
                return "Script execution started.";
            case SCRIPT_EXECUTION_COMPLETED:
                return "Script execution completed.";
            case SCRIPT_EXECUTION_FAILED:
                return "Script execution failed.";
            case SCRIPT_EXECUTION_SCHEDULED:
                return "Script execution scheduled.";
            case SCRIPT_EXECUTION_DESCHEDULED:
                return "Script execution descheduled.";
            case WEBPORTAL_CSRF_ATTACK:
                return "Webportal CSRF attack.";
            case SNMP_OTHER:
                return "SNMP other.";
            case SNMP_INFO:
                return "SNMP info.";
            case SNMP_UNSUPPORTED_VERSION:
                return "SNMP unsupported version.";
            case SNMP_UNSUPPORTED_SEC_MODEL:
                return "SNMP unsupported sec model.";
            case SNMP_INVALID_USER_NAME:
                return "SNMP invalid user name.";
            case SNMP_INVALID_ENGINE_ID:
                return "SNMP invalid engine ID.";
            case SNMP_AUTHENTICATION_FAILURE:
                return "SNMP authentication failure.";
            case SNMP_KEYS_UPDATED:
                return "SNMP keys updated.";
            case CRL_UPDATED:
                return "CRL updated.";
            case CRL_UPDATE_REJECTED:
                return "CRL update rejected.";
            case KEY_UPDATE_REQUEST:
                return "Key update request.";
            case CRL_REMOVED:
                return "CRL removed.";
            case DOT1X_SUCCESS:
                return "DOT1X success.";
            case DOT1X_FAILURE:
                return "DOT1X failure.";
            case REPLAY_ATTACK:
                return "Replay attack.";
            case CERTIFICATE_ADDED:
                return "Certificate added.";
            case CERTIFICATE_REMOVED:
                return "Certificate removed.";
            case CERTIFICATE_EXPIRED:
                return "Certificate expired.";
            // General Beacon
            case POWER_DOWN_POWER_LOST:
                return "Power down power lost.";
            case POWER_DOWN_USER_REQUEST:
                return "Power down user request.";
            case POWER_DOWN_SOFTWARE_FAULT:
                return "Power down software fault.";
            case POWER_DOWN_HARDWARE_FAULT:
                return "Power down hardware fault.";
            case POWER_DOWN_NETWORK_INACTIVITY:
                return "Power down network inactivity.";
            case POWER_DOWN_FIRMWARE_UPGRADE:
                return "Power down firmware upgrade.";
            case POWER_DOWN_FIRMWARE_ROLLBACK:
                return "Power down firmware rollback.";
            case POWER_DOWN_DISK_ERROR:
                return "Power down disk error.";
            case POWER_DOWN_CONFIGURATION_ERROR:
                return "Power down configuration error.";
            case POWER_DOWN_FACTORY_RESET:
                return "Power down factory reset.";
            case POWER_DOWN_TAMPERING:
                return "Power down tampering.";
            case POWER_DOWN_TEMPERATURE:
                return "Power down temperature.";
            case POWER_DOWN_SYSTEM_WATCHDOG:
                return "Power down system watchdog.";
            case POWER_DOWN_WWAN_MODEM_WATCHDOG:
                return "Power down WWAN modem watchdog.";
            case POWER_DOWN_SECURE_ELEMENT_WATCHDOG:
                return "Power down secure element watchdog.";
            case POWER_DOWN_EXTERNAL_WATCHDOG:
                return "Power down external watchdog.";
            case PROTOCOL_LOG_CLEARED:
                return "Protocol log cleared.";
            case METER_CLOCK_INVALID:
                return "Meter clock invalid.";
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