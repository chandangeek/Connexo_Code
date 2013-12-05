package com.energyict.smartmeterprotocolimpl.iskra.mt880.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.Date;

/**
 * @author sva
 * @since 7/10/13 - 16:34
 */
public enum MT880MeterEvent {

    FATAL_ERROR             (0x0001, MeterEvent.FATAL_ERROR, "Fatal error"),
    REPLACE_BATTERY         (0x0002, MeterEvent.REPLACE_BATTERY, "Replace battery"),
    VALUE_CORRUPT           (0x0004, MeterEvent.OTHER, "Value corrupt"),
    DST_ENABLED_OR_DISABLED (0x0008, MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED, "DST enabled or disabled"),
    BILLING_RESET           (0x0010, MeterEvent.BILLING_ACTION, "Billing reset"),
    CLOCK_ADJUSTED_BEFORE   (0x0020, MeterEvent.SETCLOCK_BEFORE, "Clock adjusted (old date/time)"),
    POWER_UP                (0x0040, MeterEvent.POWERUP, "Power Up"),
    POWER_DOWN              (0x0080, MeterEvent.POWERDOWN, "Power Down"),
    EVENT_LOG_CLEARED       (0x2000, MeterEvent.EVENT_LOG_CLEARED, "Event log cleared"),
    LOAD_PROFILE_CLEARED    (0x4000, MeterEvent.LOADPROFILE_CLEARED, "Load profile cleared"),
    POWER_DOWN_PHASE_L1     (0x8001, MeterEvent.PHASE_FAILURE, "Power down phase L1"),
    POWER_DOWN_PHASE_P2     (0x8002, MeterEvent.PHASE_FAILURE, "Power down phase L2"),
    POWER_DOWN_PHASE_L3     (0x8003, MeterEvent.PHASE_FAILURE, "Power down phase L3"),
    POWER_RESTORED_PHASE_L1 (0x8004, MeterEvent.PHASE_FAILURE, "Power restore phase L1"),
    POWER_RESTORED_PHASE_L2 (0x8005, MeterEvent.PHASE_FAILURE, "Power restore phase L2"),
    POWER_RESTORED_PHASE_L3 (0x8006, MeterEvent.PHASE_FAILURE, "Power restore phase L3"),
    METER_COVER_CLOSED      (0x800E, MeterEvent.METER_COVER_CLOSED, "Meter cover closed"),
    TERMINAL_COVER_CLOSED   (0x800F, MeterEvent.TERMINAL_COVER_CLOSED, "Terminal cover closed"),
    METER_COVER_REMOVED     (0x8010, MeterEvent.COVER_OPENED, "Meter cover removed"),
    TERMINAL_COVER_REMOVED  (0x8011, MeterEvent.TERMINAL_OPENED, "Terminal cover removed"),
    NO_CONNECTION_TIMEOUT   (0x8012, MeterEvent.OTHER, "No connection timeout"),
    VOLTAGE_SAG_PHASE_L1    (0x8020, MeterEvent.VOLTAGE_SAG, "Voltage sag phase L1"),
    VOLTAGE_SAG_PHASE_L2    (0x8021, MeterEvent.VOLTAGE_SAG, "Voltage sag phase L2"),
    VOLTAGE_SAG_PHASE_L3    (0x8022, MeterEvent.VOLTAGE_SAG, "Voltage sag phase L3"),
    VOLTAGE_SWELL_PHASE_L1  (0x8023, MeterEvent.VOLTAGE_SWELL, "Voltage swell phase L1"),
    VOLTAGE_SWELL_PHASE_L2  (0x8024, MeterEvent.VOLTAGE_SWELL, "Voltage swell phase L2"),
    VOLTAGE_SWELL_PHASE_L3  (0x8025, MeterEvent.VOLTAGE_SWELL, "Voltage swell phase L3"),
    CLOCK_ADJUSTED_AFTER    (0x8040, MeterEvent.SETCLOCK_AFTER, "Clock adjusted (new date/time)"),
    CONFIGURATION_CHANGE    (0x8041, MeterEvent.CONFIGURATIONCHANGE, "One or more parameters changed"),
    METER_MASTER_RESET      (0x8042, MeterEvent.OTHER, "Meter master reset"),
    ERROR_REGISTER_CLEARED  (0x8043, MeterEvent.ERROR_REGISTER_CLEARED, "Error register cleared"),
    ALARM_REGISTER_CLEARED  (0x8044, MeterEvent.ALARM_REGISTER_CLEARED, "Alarm register cleared"),
    PASSIVE_TOU_PROGRAMMED  (0x8045, MeterEvent.OTHER, "Passive TOU programmed"),
    TOU_ACTIVATED           (0x8046, MeterEvent.TOU_ACTIVATED, "TOU activated"),
    GLOBAL_KEY_CHANGED      (0x8047, MeterEvent.OTHER, "Global key(s) changed"),
    METER_UNLOCKED          (0x8048, MeterEvent.OTHER, "Meter unlocked"),
    METER_LOCKED            (0x8049, MeterEvent.OTHER, "Meter locked"),
    ASSOCIATION_AUTHENTICATION_FAILURE5(0x804A, MeterEvent.OTHER, "Association authentication failure (n time failed authentication)"),
    DECRYPTION_OR_AUTHENTICATION_FAILURE(0x804B, MeterEvent.OTHER, "Decryption or authentication failure (n time failure"),
    REPLAY_ATTACK           (0x804C, MeterEvent.OTHER, "Replay attack"),
    PROGRAM_MEMORY_ERROR    (0x8050, MeterEvent.PROGRAM_MEMORY_ERROR, "Program memory error"),
    RAM_ERROR               (0x8051, MeterEvent.RAM_MEMORY_ERROR, "RAM error"),
    NV_MEMORY_ERROR         (0x8052, MeterEvent.NV_MEMORY_ERROR, "NV memory error"),
    WATCHDOG_ERROR          (0x8053, MeterEvent.WATCHDOG_ERROR, "Watchdog error"),
    MEASUREMENT_SYSTEM_ERROR(0x8054, MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Measurement system error"),
    CLOCK_INVALID           (0x8055, MeterEvent.CLOCK_INVALID, "Clock invalid"),
    FIRMWARE_READY_FOR_ACTIVATION(0x8060, MeterEvent.FIRMWARE_READY_FOR_ACTIVATION, "Firmware ready for activation"),
    FIRMWARE_ACTIVATED      (0x8061, MeterEvent.FIRMWARE_ACTIVATED, "Firmware activated"),
    FW_VERIFICATION_FAILED  (0x8062, MeterEvent.OTHER, "FW verification failed"),
    PREVIOUS_VALUES_RESET   (0x8070, MeterEvent.OTHER, "Previous values reset"),
    WRONG_PHASE_SEQUENCE    (0x8071, MeterEvent.PHASE_FAILURE, "Wrong phase sequence"),
    MISSING_NEUTRAL         (0x8072, MeterEvent.PHASE_FAILURE, "Missing neutral"),
    REGISTER_ROLLOVER       (0x8073, MeterEvent.REGISTER_OVERFLOW, "Register rollover"),
    CERTIFICATION_DATA_LOG_FULL(0x8074, MeterEvent.OTHER, "Certification data log full"),
    RESULT_RESET            (0x8075, MeterEvent.OTHER, "Result reset"),
    CURRENT_WITHOUT_VOLTAGE_PHASE_L1_START(0x8080, MeterEvent.PHASE_FAILURE, "Current without voltage phase L1 - start"),
    CURRENT_WITHOUT_VOLTAGE_PHASE_L2_START(0x8081, MeterEvent.PHASE_FAILURE, "Current without voltage phase L2 - start"),
    CURRENT_WITHOUT_VOLTAGE_PHASE_L3_START(0x8082, MeterEvent.PHASE_FAILURE, "Current without voltage phase L3 - start"),
    CURRENT_WITHOUT_VOLTAGE_PHASE_L1_END(0x8083, MeterEvent.PHASE_FAILURE, "Current without voltage phase L1 - end"),
    CURRENT_WITHOUT_VOLTAGE_PHASE_L2_END(0x8084, MeterEvent.PHASE_FAILURE, "Current without voltage phase L2 - end"),
    CURRENT_WITHOUT_VOLTAGE_PHASE_L3_END(0x8085, MeterEvent.PHASE_FAILURE, "Current without voltage phase L3 - end"),
    MISSING_CURRENT_L1_START(0x8086, MeterEvent.PHASE_FAILURE, "Missing current L1 - start"),
    MISSING_CURRENT_L2_START(0x8087, MeterEvent.PHASE_FAILURE, "Missing current L2 - start"),
    MISSING_CURRENT_L3_START(0x8088, MeterEvent.PHASE_FAILURE, "Missing current L3 - start"),
    MISSING_CURRENT_L1_END  (0x8089, MeterEvent.PHASE_FAILURE, "Missing current L1 - end"),
    MISSING_CURRENT_L2_END  (0x808A, MeterEvent.PHASE_FAILURE, "Missing current L2 - end"),
    MISSING_CURRENT_L3_END  (0x808B, MeterEvent.PHASE_FAILURE, "Missing current L3 - end"),
    OVER_CURRENT_L1_START   (0x808C, MeterEvent.PHASE_FAILURE, "Over current L1 - start"),
    OVER_CURRENT_L2_START   (0x808D, MeterEvent.PHASE_FAILURE, "Over current L2 - start"),
    OVER_CURRENT_L3_START   (0x808E, MeterEvent.PHASE_FAILURE, "Over current L3 - start"),
    OVER_CURRENT_L1_END     (0x808F, MeterEvent.PHASE_FAILURE, "Over current L1 - end"),
    OVER_CURRENT_L2_END     (0x8090, MeterEvent.PHASE_FAILURE, "Over current L2 - end"),
    OVER_CURRENT_L3_END     (0x8091, MeterEvent.PHASE_FAILURE, "Over current L3 - end"),
    OVER_CURRENT_IN_NEUTRAL_START(0x8092, MeterEvent.PHASE_FAILURE, "Over current in neutral - start"),
    OVER_CURRENT_IN_NEUTRAL_END(0x8093, MeterEvent.PHASE_FAILURE, "Over current in neutral - end"),
    ASYMMETRICAL_CURRENT_START(0x8094, MeterEvent.PHASE_FAILURE, "Asymmetrical current - start"),
    ASYMMETRICAL_CURRENT_END(0x8095, MeterEvent.PHASE_FAILURE, "Asymmetrical current - end"),
    ASYMMETRICAL_VOLTAGE_START(0x8100, MeterEvent.PHASE_FAILURE, "Asymmetrical voltage - start"),
    ASYMMETRICAL_VOLTAGE_END(0x8101, MeterEvent.PHASE_FAILURE, "Asymmetrical voltage - end"),
    OVER_VOLTAGE_PHASE_L1   (0x8102, MeterEvent.PHASE_FAILURE, "Over voltage phase L1"),
    OVER_VOLTAGE_PHASE_L2   (0x8103, MeterEvent.PHASE_FAILURE, "Over voltage phase L2"),
    OVER_VOLTAGE_PHASE_L3   (0x8104, MeterEvent.PHASE_FAILURE, "Over voltage phase L3"),
    VOLTAGE_NORMAL_PHASE_L1 (0x8105, MeterEvent.PHASE_FAILURE, "Voltage normal phase L1"),
    VOLTAGE_NORMAL_PHASE_L2 (0x8106, MeterEvent.PHASE_FAILURE, "Voltage normal phase L2"),
    VOLTAGE_NORMAL_PHASE_L3 (0x8107, MeterEvent.PHASE_FAILURE, "Voltage normal phase L3"),
    UNDER_VOLTAGE_PHASE_L1  (0x8108, MeterEvent.PHASE_FAILURE, "Under voltage phase L1"),
    UNDER_VOLTAGE_PHASE_L2  (0x8109, MeterEvent.PHASE_FAILURE, "Under voltage phase L2"),
    UNDER_VOLTAGE_PHASE_L3  (0x810A, MeterEvent.PHASE_FAILURE, "Under voltage phase L3"),
    MISSING_VOLTAGE_PHASE_L1(0x810B, MeterEvent.PHASE_FAILURE, "Missing voltage phase L1"),
    MISSING_VOLTAGE_PHASE_L2(0x810C, MeterEvent.PHASE_FAILURE, "Missing voltage phase L2"),
    MISSING_VOLTAGE_PHASE_L3(0x810D, MeterEvent.PHASE_FAILURE, "Missing voltage phase L3"),
    REVERSE_POWER_FLOW_START(0x8120, MeterEvent.REVERSE_RUN, "Reverse power flow - start"),
    REVERSE_POWER_FLOW_END  (0x8121, MeterEvent.REVERSE_RUN, "Reverse power flow - end"),
    LOW_POWER_FACTOR_START  (0x8122, MeterEvent.PHASE_FAILURE, "Low power factor - start"),
    LOW_POWER_FACTOR_END    (0x8123, MeterEvent.PHASE_FAILURE, "Low power factor - end"),
    STRONG_DC_FIELD_DETECTED(0x8124, MeterEvent.STRONG_DC_FIELD_DETECTED, "Strong DC field detected"),
    NO_STRONG_DC_FIELD_ANYMORE(0x8125, MeterEvent.NO_STRONG_DC_FIELD_ANYMORE, "No strong DC field anymore"),
    ALARM_INPUT_1_ACTIVATED (0x8130, MeterEvent.OTHER, "Alarm input 1 activated"),
    ALARM_INPUT_2_ACTIVATED (0x8131, MeterEvent.OTHER, "Alarm input 2 activated"),
    ALARM_output1_ACTIVATED (0x8132, MeterEvent.OTHER, "Alarm output 1 activated"),
    ALARM_output2_ACTIVATED (0x8133, MeterEvent.OTHER, "Alarm output 2 activated"),
    LOAD_CONTROL_1_ACTIVATED(0x8140, MeterEvent.OTHER, "Load control 1 activated"),
    LOAD_CONTROL_1_DEACTIVATED(0x8141, MeterEvent.OTHER, "Load control 1 deactivated"),
    LOAD_CONTROL_2_ACTIVATED(0x8142, MeterEvent.OTHER, "Load control 2 activated"),
    LOAD_CONTROL_2_DEACTIVATED(0x8143, MeterEvent.OTHER, "Load control 2 deactivated"),
    LOAD_CONTROL_3_ACTIVATED(0x8144, MeterEvent.OTHER, "Load control 3 activated"),
    LOAD_CONTROL_3_DEACTIVATED(0x8145, MeterEvent.OTHER, "Load control 3 deactivated"),
    INITIALIZATION_FAILURE  (0x8150, MeterEvent.OTHER, "Initialization failure"),
    SIM_FAILURE             (0x8151, MeterEvent.OTHER, "SIM failure"),
    GSM_REGISTRATION_FAILURE(0x8152, MeterEvent.OTHER, "GSM registration failure"),
    GPRS_REGISTRATION_FAILURE(0x8153, MeterEvent.OTHER, "GPRS registration failure"),
    PDP_CONTEXT_ESTABLISHED (0x8154, MeterEvent.OTHER, "PDP context established"),
    PDP_CONTEXT_DESTROYED   (0x8155, MeterEvent.OTHER, "PDP context destroyed"),
    MODEM_SW_RESET          (0x8156, MeterEvent.OTHER, "Modem SW reset"),
    MODEM_HW_RESET          (0x8157, MeterEvent.OTHER, "Modem HW reset"),
    GSM_OUTGOING_CONNECTION (0x8158, MeterEvent.OTHER, "GSM outgoing connection"),
    GSM_INCOMING_CONNECTION (0x8159, MeterEvent.OTHER, "GSM incoming connection"),
    GSM_HANGUP              (0x815A, MeterEvent.OTHER, "GSM hangup"),
    DIAGNOSTIC_FAILURE      (0x815B, MeterEvent.OTHER, "Diagnostic failure"),
    USER_INITIALIZATION_FAILURE(0x815C, MeterEvent.OTHER, "User initialization failure"),
    SIGNAL_QUALITY_FAILURE  (0x815D, MeterEvent.OTHER, "Signal quality failure"),
    PDP_CONTEXT_FAILURE     (0x815E, MeterEvent.OTHER, "PDP context failure"),
    AUTO_ANSWER             (0x8160, MeterEvent.OTHER, "Auto answer"),
    NO_CONNECTION_TIMEOUT_CHANNEL_1(0x8170, MeterEvent.OTHER, "No connection timeout channel 1"),
    NO_CONNECTION_TIMEOUT_CHANNEL_2(0x8171, MeterEvent.OTHER, "No connection timeout channel 2"),
    NO_CONNECTION_TIMEOUT_CHANNEL_3(0x8172, MeterEvent.OTHER, "No connection timeout channel 3");

    private final int deviceCode;
    private final int eiserverCode;
    private final String description;

    private MT880MeterEvent(int deviceCode, int eiserverCode, String description) {
        this.deviceCode = deviceCode;
        this.eiserverCode = eiserverCode;
        this.description = description;
    }

    public int getDeviceCode() {
        return deviceCode;
    }

    public int getEiserverCode() {
        return eiserverCode;
    }

    public String getDescription() {
        return description;
    }

    public MeterEvent toMeterEvent(Date eventDate, final int logbookId, final int eventNumber) {
        return new MeterEvent(eventDate, getEiserverCode(), getDeviceCode(), getDescription(), logbookId, eventNumber);
    }

    public static MT880MeterEvent getMeterEventForDeviceCode(int deviceCode) {
        for (MT880MeterEvent event : MT880MeterEvent.values()) {
            if (event.getDeviceCode() == deviceCode) {
                return event;
            }
        }
        return null;
    }
}
