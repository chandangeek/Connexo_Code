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
import java.util.Objects;
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
    public static final int METER_COVER_OPENED = 24;        // sometimes you have two events for the tamper detection
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

    public static final int TAMPER_CLEARED = 65;
    /**
     * Technical maintenance of the meter (Comlog Code 1)
     */
    public static final int METROLOGICAL_MAINTENANCE = 71;
    /**
     * Technical maintenance of the grid (Comlog Code 2)
     */
    public static final int TECHNICAL_MAINTENANCE = 72;
    /**
     * Retrieving of meter readings E (Comlog Code 3)
     */
    public static final int RETREIVE_METER_READINGS_E = 73;
    /**
     * Retrieving of meter readings G (Comlog Code 4)
     */
    public static final int RETREIVE_METER_READINGS_G = 74;
    /**
     * Retrieving of interval data E (Comlog Code 5)
     */
    public static final int RETREIVE_INTERVAL_DATA_E = 75;
    /**
     * Retrieving of interval data E (Comlog Code 5)
     */
    public static final int RETREIVE_INTERVAL_DATA_G = 76;
    /**
     * Indicates that a short voltage sag occurred on L1 phase
     */
    public static final int SHORT_VOLTAGE_SAG_L1 = 77;
    /**
     * Indicates that a short voltage sag occurred on L2 phase
     */
    public static final int SHORT_VOLTAGE_SAG_L2 = 78;
    /**
     * Indicates that a short voltage sag occurred on L2 phase
     */
    public static final int SHORT_VOLTAGE_SAG_L3 = 79;

    public static final int SAG_PHASE_A = 80;

    public static final int SAG_PHASE_B = 81;

    public static final int SAG_PHASE_C = 82;

    public static final int SAG_PHASE_A_B = 83;

    public static final int SAG_PHASE_B_C = 84;

    public static final int SAG_PHASE_A_C = 85;

    public static final int SAG_PHASE_A_B_C = 86;

    public static final int SAG_EVENT_CLEARED = 87;

    public static final int ACCESS_READ = 88;

    public static final int ACCESS_WRITE = 89;

    public static final int PROCEDURE_INVOKED = 90;

    public static final int TABLE_WRITTEN = 91;

    public static final int COMM_TERMINATED_NORMAL = 92;

    public static final int COMM_TERMINATED_ABNORMAL = 93;

    public static final int RESET_LIST_POINTER = 94;

    public static final int UPDATE_LIST_POINTER = 95;

    public static final int HISTORY_LOG_CLEARED = 96;

    public static final int HISTORY_LOG_POINTER = 97;

    public static final int EVENT_LOG_POINTER = 98;

    public static final int SELF_READ = 99;

    public static final int DAYLIGHT_SAVING_TIME_ON = 100;

    public static final int DAYLIGHT_SAVING_TIME_OFF = 101;

    public static final int SEASON_CHANGE = 102;

    public static final int RATE_CHANGE = 103;

    public static final int SPECIAL_SCHEDULE = 104;

    public static final int TIER_SWITCH = 105;

    public static final int PENDING_TABLE_ACTIVATION = 106;

    public static final int PENDING_TALE_CLEAR = 107;

    public static final int METERING_MODE_START = 108;

    public static final int METERING_MODE_STOP = 109;

    public static final int TEST_MODE_START = 110;

    public static final int TEST_MODE_STOP = 111;

    public static final int METER_SHOP_START = 112;

    public static final int METER_SHOP_STOP = 113;

    public static final int CONFIGURATION_ERROR = 114;

    public static final int SELF_CHECK_ERROR = 115;

    public static final int RAM_FAILURE = 116;

    public static final int ROM_FAILURE = 117;

    public static final int NON_VOLATILE_MEMORY_FAILURE = 118;

    public static final int CLOCK_ERROR = 119;

    public static final int LOW_LOSS_POTENTIAL = 120;

    public static final int DEMAND_OVERLOAD = 121;

    public static final int POWER_FAILURE = 122;

    public static final int REVERSE_ROTATION = 123;

    public static final int ENTER_TIER = 124;

    public static final int EXIT_TIER = 125;

    public static final int TERMINAL_COVER_TEMPER = 126;

    public static final int MAIN_COVER_TEMPER = 127;

    public static final int EXTERNAL_EVENT = 128;

    public static final int PHASE_A_OFF = 129;

    public static final int PHASE_A_ON = 130;

    public static final int PHASE_B_OFF = 131;

    public static final int PHASE_B_ON = 132;

    public static final int PHASE_C_OFF = 133;

    public static final int PHASE_C_ON = 134;

    public static final int RWP = 135;

    public static final int DEVICE_PROGRAMMED = 136;

    public static final int REMOTE_FLASH_FAILURE = 137;

    public static final int SERVICE_VOLTAGE_TEST = 138;

    public static final int LOW_CURRENT_TEST = 139;

    public static final int POWER_FACTOR = 140;

    public static final int SECOND_HARMONIC_CURRENT_TEST = 141;

    public static final int TOTAL_HARMONIC_CURRENT = 142;

    public static final int TOTAL_HARMONIC_VOLTAGE = 143;

    public static final int VOLTAGE_IMBALANCE = 144;

    public static final int CURRENT_IMBALANCE = 145;

    public static final int TOTAL_DEMAND_DISTORTION = 146;

    public static final int HIGH_VOLTAGE_TEST = 147;

    public static final int REVERSE_POWER_TEST = 148;

    public static final int TOU_PROGRAMMED = 149;

    public static final int EXTERNAL_ALERT = 150;

    public static final int FIRMWARE_VERIFICATION_FAIL = 151;

    public static final int UNEXPECTED_CONSUMPTION = 152;

    public static final int PHASE_REVERSAL = 153;

    public static final int MISSING_NEUTRAL = 154;

    public static final int N_TIMES_DECRYPT_FAIL = 155;

    public static final int DISCONNECTOR_READY_FOR_RECONN = 156;

    public static final int DISCONNECT_RECONNECT_FAIL = 157;

    public static final int LOCAL_RECONNECTION = 158;

    public static final int SUPERVISION_1_EXCEEDED = 159;

    public static final int SUPERVISION_1_OK = 160;

    public static final int SUPERVISION_2_EXCEEDED = 161;

    public static final int SUPERVISION_2_OK = 162;

    public static final int SUPERVISION_3_EXCEEDED = 163;

    public static final int SUPERVISION_3_OK = 164;

    public static final int PHASE_ASYMMETRY = 165;

    public static final int COMMUNICATION_TIMEOUT = 166;

    public static final int MODEM_INITIALIZATION_FAIL = 167;

    public static final int SIM_CARD_FAIL = 168;

    public static final int SIM_CARD_OK = 169;

    public static final int GSM_GPRS_REGISTRATION_FAIL = 170;

    public static final int PDP_CONTEXT_ESTABLISHED = 171;

    public static final int PDP_CONTEXT_DESTROYED = 172;

    public static final int PDP_CONTEXT_FAIL = 173;

    public static final int MODEM_SW_RESET = 174;

    public static final int MODEM_HW_RESET = 175;

    public static final int GSM_CONNECTION = 176;

    public static final int GSM_HANG_UP = 177;

    public static final int DIAGNOSTIC_FAILURE = 178;

    public static final int USER_INITIALIZATION_FAIL = 179;

    public static final int ANSWER_NUMBER_EXCEEDED = 180;

    public static final int LOCAL_COMMUNICATION_ATTEMPT = 181;

    public static final int GLOBAL_KEY = 182;

    public static final int UNDERVOLTAGE_L1 = 183;

    public static final int UNDERVOLTAGE_L2 = 184;

    public static final int UNDERVOLTAGE_L3 = 185;

    public static final int OVERVOLTAGE_L1 = 186;

    public static final int OVERVOLTAGE_L2 = 187;

    public static final int OVERVOLTAGE_L3 = 188;

    public static final int MISSINGVOLTAGE_L1 = 189;

    public static final int MISSINGVOLTAGE_L2 = 190;

    public static final int MISSINGVOLTAGE_L3 = 191;

    public static final int NORMALVOLTAGE_L1 = 192;

    public static final int NORMALVOLTAGE_L2 = 193;

    public static final int NORMALVOLTAGE_L3 = 194;

    public static final int BADVOLTAGE_L1 = 195;

    public static final int BADVOLTAGE_L2 = 196;

    public static final int BADVOLTAGE_L3 = 197;

    public static final int SIGNAL_QUALITY_LOW = 198;

    public static final int METER_ALARM_END = 199;

    public static final int REVERSE_POWER = 200;

    public static final int INPUT_EVENT = 201;

    public static final int CHANGE_IMPULSE = 202;

    public static final int PLUS_A_STORED = 203;

    public static final int PARAMETER_RESTORED = 204;

    public static final int PARAMETER_INITIALIZED = 205;

    public static final int STATUS_CHANGED = 206;

    public static final int RELAY_CONNECTED = 207;

    public static final int RELAY_DISCONNECTED = 208;

    public static final int RELAY_CONNECT_FAILED = 209;

    public static final int RELAY_DISCONNECT_FAILED = 210;

    public static final int SESSION_STARTED = 211;

    public static final int CONFIGURATION_PARAMETER_CHANGED = 212;

    public static final int SESSION_STOPPED = 213;

    public static final int SESSION_TERMINATED = 214;

    public static final int SESSION_EXPIRED = 215;

    public static final int SESSION_DISALLOWED = 216;

    public static final int SESSION_SUBSTITUTED = 217;

    public static final int SESSION_ABORTED = 218;

    public static final int BATTERY_STATUS_ENABLED = 219;

    public static final int BATTERY_STATUS_DISABLED = 220;

    public static final int ASS_DEVICE_INPUT_ERROR = 221;

    public static final int MODEM_SESSION_FAILED = 222;

    public static final int LOG_RESET = 223;
    public static final int LOCAL_COMM_START = 224;
    public static final int LOCAL_COMM_END = 225;
    public static final int NOT_METRO_PARAM_CONF = 226;
    public static final int GAS_FLOW_RATE_ABOVE_THRESHOLD_START = 227;
    public static final int GAS_FLOW_RATE_ABOVE_THRESHOLD_END = 228;
    public static final int GAS_REVERSE_FLOW_START = 229;
    public static final int GAS_REVERSE_FLOW_END = 230;
    public static final int GAS_TEMP_ABOVE_PHYSICAL_THRESHOLD_START = 231;
    public static final int GAS_TEMP_ABOVE_PHYSICAL_THRESHOLD_END = 232;
    public static final int GAS_TEMP_BELOW_PHYSICAL_THRESHOLD_START = 233;
    public static final int GAS_TEMP_BELOW_PHYSICAL_THRESHOLD_END = 234;
    public static final int TEMP_FAILURE_START = 235;
    public static final int TEMP_FAILURE_END = 236;
    public static final int PASSWORD_CHANGED = 237;
    public static final int BATTERY_LEVEL_BELOW_LOW_LEVEL_END = 238;
    public static final int REMOTE_COMM_FAILURE = 239;
    public static final int PUSH_ERROR_START = 240;
    public static final int BATTERY_BELOW_CRITICAL_LEVEL = 241;
    public static final int FIRMWARE_UPDATE_ACTIVATION_FAILURE = 242;
    public static final int PHYSICAL_MODULE_DISCONNECT = 243;
    public static final int UNAUTHORIZED_ACCESS = 244;
    public static final int DATABASE_RESET_AFTER_UPDATE = 245;
    public static final int POWER_LEVEL_INCREASED = 246;
    public static final int POWER_LEVEL_DECREASED = 247;
    public static final int POWER_LEVEL_MAXIMUM_REACHED = 248;
    public static final int POWER_LEVEL_MINIMUM_REACHED = 249;
    public static final int PM1_CHANNEL_CHANGED = 250;
    public static final int PM1_ACTIVE_MODE_START = 251;
    public static final int PM1_ACTIVE_MODE_END = 252;
    public static final int PM1_ORPHANED_MODE_START = 253;
    public static final int PM1_ORPHANED_MODE_END = 254;
    public static final int PM1_PIB_UPDATED = 255;
    public static final int PM1_MIB_UPDATED = 256;
    public static final int PM1_SYNC_ACCESS_CHANGED = 257;
    public static final int PM1_SYNC_PERIOD_CHANGED = 258;
    public static final int PM1_MAINTENANCE_WINDOW_CHANGED = 259;
    public static final int PM1_ORPHANED_THRESHOLD_CHANGED = 260;
    public static final int PM1_AFFILIATION_PARAMS_CHANGED = 261;
    public static final int SECONDARY_ADDRESS_RF_CHANGED = 262;
    public static final int VALVE_PGV_CONFIGURATION_CHANGED = 263;
    public static final int PUSH_SCHEDULER1_CHANGED = 264;
    public static final int PUSH_SETUP1_CHANGED = 265;
    public static final int PUSH_SCHEDULER2_CHANGED = 266;
    public static final int PUSH_SETUP2_CHANGED = 267;
    public static final int PUSH_SCHEDULER3_CHANGED = 268;
    public static final int PUSH_SETUP3_CHANGED = 269;
    public static final int PUSH_SCHEDULER4_CHANGED = 270;
    public static final int PUSH_SETUP4_CHANGED = 271;
    public static final int ENABLING_INSTALLER_MANTAINER = 272;
    public static final int FC_THRESHOLDS_CHANGED = 273;
    public static final int REMOTE_CONNECTION_START = 274;
    public static final int MAINTENANCE_WINDOW_HW_FAILURE = 275;
    public static final int MAINTENANCE_WINDOW_SW_FAILURE = 276;
    public static final int MAINTENANCE_WINDOW_START = 277;
    public static final int MAINTENANCE_WINDOW_END = 278;
    public static final int ASSOCIATION_INSTALLER_DISABLED = 279;
    public static final int VALVE_POSITION_ERROR = 280;
    public static final int VALVE_ENABLE_OPENING = 281;
    public static final int MISER_MODE_FAILURE = 282;
    public static final int DEVICE_RESET = 283;
    public static final int METROLOGIC_RESET = 284;
    public static final int ACTIVATION_NEW_TARIFF_PLAN = 285;
    public static final int PLANNING_NEW_TARIFF_PLAN = 286;
    public static final int CLOCK_SYNC_FAIL = 287;
    public static final int CLOCK_SYNC = 288;
    public static final int METROLOGICAL_PARAMETER_CONFIGURATION = 289;
    public static final int MEASURE_ALGORITHM_ERROR_START = 290;
    public static final int MEASURE_ALGORITHM_ERROR_END = 291;
    public static final int GENERAL_ERROR_DEVICE_START = 292;
    public static final int GENERAL_ERROR_DEVICE_END = 293;
    public static final int BUFFER_FULL = 294;
    public static final int BUFFER_ALMOST_FULL = 295;
    public static final int VALVE_CLOSED_BECAUSE_OF_COMMAND = 296;
    public static final int VALVE_OPENED = 297;
    public static final int MEMORY_FAILURE = 298;
    public static final int UNITS_STATUS_CHANGED = 299;
    public static final int MAIN_POWER_OUTAGE_START = 300;
    public static final int MAIN_POWER_OUTAGE_END = 301;
    public static final int BATTERY_LEVEL_BELOW_LOW_LEVEL_START = 302;
    public static final int DEVICE_TAMPER_DETECTION_START = 303;
    public static final int DEVICE_TAMPER_DETECTION_END = 304;
    public static final int CRITICAL_SOFTWARE_ERROR = 305;
    public static final int DST_START = 306;
    public static final int DST_END = 307;
    public static final int BILLING_PERIOD_CLOSING_LOCAL_REQUEST = 308;
    public static final int BILLING_PERIOD_CLOSING_REMOTE_REQUEST = 309;
    public static final int BATTERY_ABOVE_CRITICAL_LEVEL = 310;
    public static final int FIRMWARE_UPDATE_START = 311;
    public static final int FIRMWARE_UPDATE_DATE_ACTIVATION = 312;
    public static final int FIRMWARE_UPDATE_VERIFY_OK = 313;
    public static final int FIRMWARE_UPDATE_VERIFY_FAILURE = 314;
    public static final int FIRMWARE_UPDATE_ACTIVATION_OK = 315;
    public static final int CLOSE_VALVE_LEAKAGE_CAUSE = 316;
    public static final int CLOSE_VALVE_BATTERY_REMOVED_WITH_NO_AUTH = 317;
    public static final int CLOSE_VALVE_BATTERY_BELOW_CRITICAL_POINT = 318;
    public static final int CLOSE_VALVE_MEASURE_FAILURE = 319;
    public static final int VALVE_PASSWORD_INVALID = 320;
    public static final int CLOSE_VALVE_COMMUNICATION_TIMEOUT = 321;
    public static final int VALVE_NEW_PASSWORD = 322;
    public static final int VALVE_READY_PASSWORD_VALID = 323;
    public static final int VALVE_READY_CONNECTION_OK = 324;
    public static final int VALVE_RECONNECT_START = 325;
    public static final int VALVE_RECONNECT_END = 326;
    public static final int VALVE_IS_CLOSED_BUT_LEAKAGE_IS_PRESENT = 327;
    public static final int VALVE_CANNOT_OPEN_CLOSE = 328;
    public static final int EXTERNAL_FIELD_APPLICATION_INTERFERING_START = 329;
    public static final int EXTERNAL_FIELD_APPLICATION_INTERFERING_END = 330;
    public static final int ACCESS_TO_ELECTRONIC = 331;
    public static final int UNAUTHORIZED_BATTERY_REMOVE = 332;
    public static final int DATABASE_RESET = 333;
    public static final int DATABASE_CORRUPTED = 334;
    public static final int UPDATED_MASTERKEY = 335;
    public static final int UPDATED_KEYC = 336;
    public static final int UPDATED_KEYT = 337;
    public static final int UPDATED_KEYS = 338;
    public static final int UPDATED_KEYN = 339;
    public static final int UPDATED_KEYM = 340;
    public static final int GAS_DAY_UPDATED = 341;
    public static final int BILLING_PERIOD_UPDATED = 342;
    public static final int INSTALLER_MAINTAINER_USER_CHANGED = 343;
    public static final int CLOCK_PARAMETER_SCHANGED = 344;
    public static final int SYNC_ALGORITHM_CHANGED = 345;
    public static final int PDR_CHANGED = 346;
    public static final int DEFAULT_TEMPERATURE = 347;
    public static final int FALLBACK_TEMPERATURE_CHANGED = 348;
    public static final int VALVE_CLOSE_FOR_MAX_FRAUD_ATTEMPTS = 349;
    public static final int VALVE_CLOSE_FOR_EXCEEDED_BATTERY_REMOVAL_TIME = 350;
    public static final int VALVE_CONFIGURATION_PGV_BIT8_CHANGED = 351;

    public static final int TOO_HIGH_CONSUMPTION_OR_PRODUCTION = 352;

    public static final int INDEX_VALUE_DECREASE_OR_RESET = 353;

    public static final int MISMATCH_BETWEEN_TOTAL_AND_TARIFF_REGISTERS = 354;

    public static final int TAMPER_BATTERY = 355;

    public static final int HLC_DAMAGE = 356;

    public static final int PERMANENT_LOG_FILLED_UP_90_PERSENT = 357;

    public static final int DEVICE_ABOUT_HIBERNATION_MODE = 358;

    public static final int SEND_FRAME_COUNTER_ABOVE_THRESHOLD = 359;

    public static final int RECEIVE_FRAME_COUNTER_ABOVE_THRESHOLD = 360;

    public static final int POWER_FAIL = 361;

    public static final int MAX_FLOW = 362;

    public static final int TEMP_MIN_LIMIT = 363;

    public static final int TEMP_MAX_LIMIT = 364;

    public static final int PULSE_ERROR = 365;

    public static final int CONSUMPTION_ERROR = 366;

    public static final int BATTERY_CONSUMPTION_HIGH = 367;

    public static final int REVERSE_FLOW = 368;

    public static final int TAMPER_P2 = 369;

    public static final int TAMPER_P0 = 370;

    public static final int TAMPER_CASE = 371;

    public static final int SYSTEM_HW_ERROR = 372;

    public static final int CFG_CALIBRATION_ERROR = 373;

    public static final int TEMPERATURE_SENSOR_ERROR = 374;

    public static final int BINDING_FLAG = 375;

    public static final int EOB_RESET = 376;
    public static final int MANUAL_RESET = 377;
    public static final int AUTO_RESET = 378;
    public static final int ROLL_OVER_TO_ZERO = 379;
    public static final int SELECTION_OF_INPUTS_SIGNALS = 380;
    public static final int OUTPUT_RELAY_CONTROL_SIGNALS_STATE_CHANGE = 381;
    public static final int ERROR_REGISTR_1_CHANGED = 382;
    public static final int ERROR_REGISTR_2_CHANGED = 383;
    public static final int ERROR_REGISTR_3_CHANGED = 384;
    public static final int COMMUNICATION_STATUS_CHANGED = 385;
    public static final int MAXIMUM_CURRENT = 386;
    public static final int SAG_CONFIRMED = 387;
    public static final int SWELL_CONFIRMED = 388;
    public static final int RECOVERY_AFTER_CURRENT_OVERLIMIT = 389;
    public static final int RECOVERY_TIMES_SETTING_CHANGED = 390;
    public static final int RECOVERY_MECHANISM_RELEASED = 391;
    public static final int DISCONNECTOR_STATUS_CHANGED = 392;
    public static final int SECURITY_EVENT = 393;
    public static final int MAXIMUM_DEMAND_EVENT = 394;

    public static final int TIME_BEFORE_CHANGE = 395;
    public static final int TIME_AFTER_CHANGE = 396;
    public static final int COMM_PORT_STATUS_CHANGE = 397;
    public static final int OUTPUT_VALVE_CONTROL = 398;

    public static final int END_OF_NONPERIODIC_BILLING_INTERVAL = 399;
    public static final int LOADPROFILE_1_CAPTURING_ENABLED = 400;
    public static final int LOADPROFILE_1_CAPTURING_DISABLED = 401;
    public static final int LOADPROFILE_2_CAPTURING_ENABLED = 402;
    public static final int LOADPROFILE_2_CAPTURING_DISABLED = 403;
    public static final int FOTA_UPGRADING_INITIATED = 404;
    public static final int FOTA_UPGRADING_FINISH_SUCCESS = 405;
    public static final int FOTA_UPGRADING_FAILED = 406;
    public static final int TARIFF_SCHEME_CHANGED = 407;
    public static final int START_CERTIFICATION_MODE = 408;
    public static final int END_CERTIFICATION_MODE = 409;
    public static final int LOAD_MANAGEMENT_PASSIVE_CALENDAR_ACTIVATED = 410;
    public static final int LOAD_MANAGEMENT_ACTIVITY_CALENDAR_PROGRAMMED = 411;
    public static final int CONSUMER_MESSAGE_UPDATE = 412;
    public static final int CONSUMER_INTERFACE_DEACTIVATED = 413;
    public static final int CONSUMER_INTERFACE_REACTIVATED = 414;
    public static final int MODEM_REMOVED = 415;
    public static final int PLC_G3_PSK_CHANGE = 416;
    public static final int LOAD_RELAY_CHANGE_REQUEST_REMOTE = 417;
    public static final int REINITIALIZATION_RNG = 418;
    public static final int LOCAL_INTERFACE_DEACTIVATED = 419;
    public static final int LOCAL_INTERFACE_REACTIVATED = 420;
    public static final int CURRENT_REVERSAL = 421;
    public static final int COMMUNICATION_STARTED_ON_REMOTE_INTERFACE_LAN_WAN = 422;
    public static final int COMMUNICATION_ENDED_ON_REMOTE_INTERFACE_LAN_WAN = 423;
    public static final int COMMUNICATION_STARTED_ON_LOCAL_INTERFACE_WZ = 424;
    public static final int COMMUNICATION_ENDED_ON_LOCAL_INTERFACE_WZ = 425;
    public static final int PASSIVE_ACTIVITY_CALENDAR_ACTIVATED = 426;
    public static final int ACTIVITY_CALENDAR_PROGRAMMED = 427;
    public static final int MODEM_FAILS_RESPONSE = 428;
    public static final int SIGNAL_QUALITY_RESTORED = 429;
    public static final int TCP_IP_CONNECTION_ESTABLISHMENT_FAILURE = 430;
    public static final int ACK_NOT_RECEIVED = 431;
    public static final int PING_RESPONSE_NOT_RECEIVED = 432;
    public static final int METER_RECORDS_CONTRACTOR_CHANGED_TO_ARMED_STATUS = 433;
    public static final int COMMS_HUB_PRESENT = 434;
    public static final int COMMS_HUB_REMOVED = 435;
    public static final int CT_VT_RATIO_CHANGED = 436;

    public static final int SOFTWARE_RESTART_REQUEST = 10001;
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
    public static final int MAX_NUMBER_OF_EVENTS = 204;

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

    public static String codeToMessage(int code) {
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
            case METER_COVER_OPENED:
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
            case TAMPER_BATTERY:
                return ("Tamper Battery");
            case HLC_DAMAGE:
                return ("HLC damage");
            case PERMANENT_LOG_FILLED_UP_90_PERSENT:
                return ("Permanent Log filled up to 90%");
            case DEVICE_ABOUT_HIBERNATION_MODE:
                return ("Device is about to enter hibernation mode");
            case SEND_FRAME_COUNTER_ABOVE_THRESHOLD:
                return ("Send Frame Counter above threshold");
            case RECEIVE_FRAME_COUNTER_ABOVE_THRESHOLD:
                return ("Receive Fame Counter above threshold");
            case POWER_FAIL:
                return ("Powerfail");
            case MAX_FLOW:
                return ("Max Flow");
            case TEMP_MIN_LIMIT:
                return ("TempMinLimit");
            case TEMP_MAX_LIMIT:
                return ("TempMaxLimit");
            case PULSE_ERROR:
                return ("Pulse error");
            case CONSUMPTION_ERROR:
                return ("Consumption Error");
            case BATTERY_CONSUMPTION_HIGH:
                return ("Battery low according to customer setting and expected battery lifetime");
            case REVERSE_FLOW:
                return ("Reverse flow above configured limit");
            case TAMPER_P2:
                return ("Communication Tamper. Checked at every encrypted M-Bus telegram");
            case TAMPER_P0:
                return ("Not used within ESMR spec (No active P0 port on M-Bus device)");
            case TAMPER_CASE:
                return ("Mechanical tamper of cover");
            case SYSTEM_HW_ERROR:
                return ("Regular check of SW/HW (selfcheck failure)");
            case CFG_CALIBRATION_ERROR:
                return ("Set if configuration is changed.");
            case TEMPERATURE_SENSOR_ERROR:
                return ("Temp sensor is broken");
            case BINDING_FLAG:
                return ("The binding flag is set by the M-Bus device when the device is in installation mode and it has been bound to an E-meter (it received the for this specific M-Bus device intended CNF_IR).");
            case EOB_RESET:
                return ("EOB Reset");
            case MANUAL_RESET:
                return ("Manual Reset");
            case AUTO_RESET:
                return ("Auto Reset");
            case ROLL_OVER_TO_ZERO:
                return ("Roll Over To Zero");
            case SELECTION_OF_INPUTS_SIGNALS:
                return ("Selection Of Input Signals");
            case OUTPUT_RELAY_CONTROL_SIGNALS_STATE_CHANGE:
                return ("Output Relay Control Signals State Change");
            case ERROR_REGISTR_1_CHANGED:
                return ("Error Register 1 Changed");
            case ERROR_REGISTR_2_CHANGED:
                return ("Error Register 2 Changed");
            case ERROR_REGISTR_3_CHANGED:
                return ("Error Register 3 Changed");
            case COMMUNICATION_STATUS_CHANGED:
                return ("Communication Status Changed");
            case MAXIMUM_CURRENT:
                return ("Maximum Current");
            case SAG_CONFIRMED:
                return ("Sag Confirmed");
            case SWELL_CONFIRMED:
                return ("Swell Confirmed");
            case RECOVERY_AFTER_CURRENT_OVERLIMIT:
                return ("Consumption Error");
            case RECOVERY_TIMES_SETTING_CHANGED:
                return ("Recovery Mechanism Changed");
            case RECOVERY_MECHANISM_RELEASED:
                return ("Recovery Mechanism Released");
            case DISCONNECTOR_STATUS_CHANGED:
                return ("Disconnector Status Changed");
            case SECURITY_EVENT:
                return ("Security Event");
            case MAXIMUM_DEMAND_EVENT:
                return ("MAximum Demand Event");
            case TIME_BEFORE_CHANGE:
                return ("Time Before Change");
            case TIME_AFTER_CHANGE:
                return ("Time After Change");
            case COMM_PORT_STATUS_CHANGE:
                return ("Communication Port Status Change");
            case OUTPUT_VALVE_CONTROL:
                return ("Output Valve Control");
            case  TOO_HIGH_CONSUMPTION_OR_PRODUCTION:
                return ("Too high consumption or production of energy.");
            case INDEX_VALUE_DECREASE_OR_RESET:
                return ("Decreasing index values or reset of index values.");
            case MISMATCH_BETWEEN_TOTAL_AND_TARIFF_REGISTERS:
                return ("Mismatch between total registers and tariff registers.");
            case LOG_RESET:
                return ("Log reset");
            case LOCAL_COMM_START:
                return ("Local communication start");
            case LOCAL_COMM_END:
                return ("Local communication end");
            case NOT_METRO_PARAM_CONF:
                return ("Not metrological parameter configuration");
            case GAS_FLOW_RATE_ABOVE_THRESHOLD_START:
                return ("Gas flow rate above threshold start");
            case GAS_FLOW_RATE_ABOVE_THRESHOLD_END:
                return ("Gas flow rate above threshold end");
            case GAS_REVERSE_FLOW_START:
                return ("Gas reverse flow start");
            case GAS_REVERSE_FLOW_END:
                return ("Gas reverse flow end");
            case GAS_TEMP_ABOVE_PHYSICAL_THRESHOLD_START:
                return ("Gas temperature above physical threshold start");
            case GAS_TEMP_ABOVE_PHYSICAL_THRESHOLD_END:
                return ("Gas temperature above physical threshold end");
            case GAS_TEMP_BELOW_PHYSICAL_THRESHOLD_START:
                return ("Gas temperature below physical threshold start");
            case GAS_TEMP_BELOW_PHYSICAL_THRESHOLD_END:
                return ("Gas temperature below physical threshold end");
            case TEMP_FAILURE_START:
                return ("Temperature failure start");
            case TEMP_FAILURE_END:
                return ("Temperature failure end");
            case PASSWORD_CHANGED:
                return ("Password changed");
            case BATTERY_LEVEL_BELOW_LOW_LEVEL_END:
                return ("Battery level below low level end");
            case REMOTE_COMM_FAILURE:
                return ("Remote communication failure");
            case PUSH_ERROR_START:
                return ("Push error start");
            case BATTERY_BELOW_CRITICAL_LEVEL:
                return ("Battery below critical level");
            case FIRMWARE_UPDATE_ACTIVATION_FAILURE:
                return ("Firmware update activation failure");
            case PHYSICAL_MODULE_DISCONNECT:
                return ("Physical module disconnect");
            case UNAUTHORIZED_ACCESS:
                return ("Unauthorized access");
            case DATABASE_RESET_AFTER_UPDATE:
                return ("Database reset after update");
            case POWER_LEVEL_INCREASED:
                return ("Power level increased");
            case POWER_LEVEL_DECREASED:
                return ("Power level decreased");
            case POWER_LEVEL_MAXIMUM_REACHED:
                return ("Power level maximum reached");
            case POWER_LEVEL_MINIMUM_REACHED:
                return ("Power level minimum reached");
            case PM1_CHANNEL_CHANGED:
                return ("Pm1 channel changed");
            case PM1_ACTIVE_MODE_START:
                return ("Pm1 active mode start");
            case PM1_ACTIVE_MODE_END:
                return ("Pm1 active mode end");
            case PM1_ORPHANED_MODE_START:
                return ("Pm1 orphaned mode start");
            case PM1_ORPHANED_MODE_END:
                return ("Pm1 orphaned mode end");
            case PM1_PIB_UPDATED:
                return ("Pm1 pib updated");
            case PM1_MIB_UPDATED:
                return ("Pm1 mib updated");
            case PM1_SYNC_ACCESS_CHANGED:
                return ("Pm1 sync access changed");
            case PM1_SYNC_PERIOD_CHANGED:
                return ("Pm1 sync period changed");
            case PM1_MAINTENANCE_WINDOW_CHANGED:
                return ("Pm1 maintenance window changed");
            case PM1_ORPHANED_THRESHOLD_CHANGED:
                return ("Pm1 orphaned threshold changed");
            case PM1_AFFILIATION_PARAMS_CHANGED:
                return ("Pm1 affiliation params changed");
            case SECONDARY_ADDRESS_RF_CHANGED:
                return ("Secondary address rf changed");
            case VALVE_PGV_CONFIGURATION_CHANGED:
                return ("Valve pgv configuration changed");
            case PUSH_SCHEDULER1_CHANGED:
                return ("Push scheduler 1 changed");
            case PUSH_SETUP1_CHANGED:
                return ("Push setup 1 changed");
            case PUSH_SCHEDULER2_CHANGED:
                return ("Push scheduler 2 changed");
            case PUSH_SETUP2_CHANGED:
                return ("Push setup 2 changed");
            case PUSH_SCHEDULER3_CHANGED:
                return ("Push scheduler 3 changed");
            case PUSH_SETUP3_CHANGED:
                return ("Push setup 3 changed");
            case PUSH_SCHEDULER4_CHANGED:
                return ("Push scheduler 4 changed");
            case PUSH_SETUP4_CHANGED:
                return ("Push setup 4 changed");
            case ENABLING_INSTALLER_MANTAINER:
                return ("Enabling installer mantainer");
            case FC_THRESHOLDS_CHANGED:
                return ("Fc thresholds changed");
            case REMOTE_CONNECTION_START:
                return ("Remote connection start");
            case MAINTENANCE_WINDOW_HW_FAILURE:
                return ("Maintenance window hw failure");
            case MAINTENANCE_WINDOW_SW_FAILURE:
                return ("Maintenance window sw failure");
            case MAINTENANCE_WINDOW_START:
                return ("Maintenance window start");
            case MAINTENANCE_WINDOW_END:
                return ("Maintenance window end");
            case ASSOCIATION_INSTALLER_DISABLED:
                return ("Association installer disabled");
            case VALVE_POSITION_ERROR:
                return ("Valve position error");
            case VALVE_ENABLE_OPENING:
                return ("Valve enable opening");
            case MISER_MODE_FAILURE:
                return ("Miser mode failure");
            case DEVICE_RESET:
                return ("Device reset");
            case METROLOGIC_RESET:
                return ("Metrologic reset");
            case ACTIVATION_NEW_TARIFF_PLAN:
                return ("Activation new tariff plan");
            case PLANNING_NEW_TARIFF_PLAN:
                return ("Planning new tariff plan");
            case CLOCK_SYNC_FAIL:
                return ("Clock sync fail");
            case CLOCK_SYNC:
                return ("Clock sync");
            case METROLOGICAL_PARAMETER_CONFIGURATION:
                return ("Metrological parameter configuration");
            case MEASURE_ALGORITHM_ERROR_START:
                return ("Measure algorithm error start");
            case MEASURE_ALGORITHM_ERROR_END:
                return ("Measure algorithm error end");
            case GENERAL_ERROR_DEVICE_START:
                return ("General error device start");
            case GENERAL_ERROR_DEVICE_END:
                return ("General error device end");
            case BUFFER_FULL:
                return ("Buffer full");
            case BUFFER_ALMOST_FULL:
                return ("Buffer almost full");
            case VALVE_CLOSED_BECAUSE_OF_COMMAND:
                return ("Valve closed because of command");
            case VALVE_OPENED:
                return ("Valve opened");
            case MEMORY_FAILURE:
                return ("Memory failure");
            case UNITS_STATUS_CHANGED:
                return ("Uni ts status changed");
            case MAIN_POWER_OUTAGE_START:
                return ("Main power outage start");
            case MAIN_POWER_OUTAGE_END:
                return ("Main power outage end");
            case BATTERY_LEVEL_BELOW_LOW_LEVEL_START:
                return ("Battery level below low level start");
            case DEVICE_TAMPER_DETECTION_START:
                return ("Device tamper detection start");
            case DEVICE_TAMPER_DETECTION_END:
                return ("Device tamper detection end");
            case CRITICAL_SOFTWARE_ERROR:
                return ("Critical software error");
            case DST_START:
                return ("Dst start");
            case DST_END:
                return ("Dst end");
            case BILLING_PERIOD_CLOSING_LOCAL_REQUEST:
                return ("Billing period closing local request");
            case BILLING_PERIOD_CLOSING_REMOTE_REQUEST:
                return ("Billing period closing remote request");
            case BATTERY_ABOVE_CRITICAL_LEVEL:
                return ("Battery above critical level");
            case FIRMWARE_UPDATE_START:
                return ("Firmware update start");
            case FIRMWARE_UPDATE_DATE_ACTIVATION:
                return ("Firmware update date activation");
            case FIRMWARE_UPDATE_VERIFY_OK:
                return ("Firmware update verify ok");
            case FIRMWARE_UPDATE_VERIFY_FAILURE:
                return ("Firmware update verify failure");
            case FIRMWARE_UPDATE_ACTIVATION_OK:
                return ("Firmware update activation ok");
            case CLOSE_VALVE_LEAKAGE_CAUSE:
                return ("Close valve leakage cause");
            case CLOSE_VALVE_BATTERY_REMOVED_WITH_NO_AUTH:
                return ("Close valve battery removed with no auth");
            case CLOSE_VALVE_BATTERY_BELOW_CRITICAL_POINT:
                return ("Close valve battery below critical point");
            case CLOSE_VALVE_MEASURE_FAILURE:
                return ("Close valve measure failure");
            case VALVE_PASSWORD_INVALID:
                return ("Valve password invalid");
            case CLOSE_VALVE_COMMUNICATION_TIMEOUT:
                return ("Close valve communication timeout");
            case VALVE_NEW_PASSWORD:
                return ("Valve new password");
            case VALVE_READY_PASSWORD_VALID:
                return ("Valve ready password valid");
            case VALVE_READY_CONNECTION_OK:
                return ("Valve ready connection ok");
            case VALVE_RECONNECT_START:
                return ("Valve reconnect start");
            case VALVE_RECONNECT_END:
                return ("Valve reconnect end");
            case VALVE_IS_CLOSED_BUT_LEAKAGE_IS_PRESENT:
                return ("Valve is closed but leakage is present");
            case VALVE_CANNOT_OPEN_CLOSE:
                return ("Valve cannot open close");
            case EXTERNAL_FIELD_APPLICATION_INTERFERING_START:
                return ("External field application interfering start");
            case EXTERNAL_FIELD_APPLICATION_INTERFERING_END:
                return ("External field application interfering end");
            case ACCESS_TO_ELECTRONIC:
                return ("Access to electronic");
            case UNAUTHORIZED_BATTERY_REMOVE:
                return ("Unauthorized battery remove");
            case DATABASE_RESET:
                return ("Database reset");
            case DATABASE_CORRUPTED:
                return ("Database corrupted");
            case UPDATED_MASTERKEY:
                return ("Updated master key");
            case UPDATED_KEYC:
                return ("Updated keyc");
            case UPDATED_KEYT:
                return ("Updated keyt");
            case UPDATED_KEYS:
                return ("Updated keys");
            case UPDATED_KEYN:
                return ("Updated keyn");
            case UPDATED_KEYM:
                return ("Updated keym");
            case GAS_DAY_UPDATED:
                return ("Gas day updated");
            case BILLING_PERIOD_UPDATED:
                return ("Billing period updated");
            case INSTALLER_MAINTAINER_USER_CHANGED:
                return ("Installer mantainer user changed");
            case CLOCK_PARAMETER_SCHANGED:
                return ("Clock parameters changed");
            case SYNC_ALGORITHM_CHANGED:
                return ("Sync algorithm changed");
            case PDR_CHANGED:
                return ("Pdr changed");
            case DEFAULT_TEMPERATURE:
                return ("Default temperature changed");
            case FALLBACK_TEMPERATURE_CHANGED:
                return ("Fallback temperature changed");
            case VALVE_CLOSE_FOR_MAX_FRAUD_ATTEMPTS:
                return ("Valve close for max fraud attempts");
            case VALVE_CLOSE_FOR_EXCEEDED_BATTERY_REMOVAL_TIME:
                return ("Valve close for exceeded battery removal time");
            case VALVE_CONFIGURATION_PGV_BIT8_CHANGED:
                return ("Valve configuration pgv bit 8 changed");
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeterEvent that = (MeterEvent) o;
        return eiCode == that.eiCode &&
                protocolCode == that.protocolCode &&
                eventLogId == that.eventLogId &&
                deviceEventId == that.deviceEventId &&
                Objects.equals(time, that.time) &&
                Objects.equals(message, that.message) &&
                Objects.equals(additionalInfo, that.additionalInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, eiCode, protocolCode, message, eventLogId, deviceEventId, additionalInfo);
    }
}