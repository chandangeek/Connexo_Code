package com.energyict.mdc.protocol.api.cim;

import com.elster.jupiter.metering.events.EndDeviceEventType;

/**
 * Enum containing the mapping between the {@link com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent}s
 * EIServer code and their corresponding CIM {@link EndDeviceEventType}.
 *
 * @author sva
 * @since 3/06/13 - 15:00
 */
public enum EndDeviceEventTypeMapping {

    OTHER                   (0, "0.0.0.0"),
    POWERDOWN               (1, "0.26.38.47"),
    POWERUP                 (2, "0.26.38.49"),
    WATCHDOGRESET           (3, "0.11.3.215"),
    SETCLOCK_BEFORE         (4, "0.36.116.14"),
    SETCLOCK_AFTER          (5, "0.36.116.24"),
    SETCLOCK                (6, "0.36.116.13"),
    CONFIGURATIONCHANGE     (7, "0.7.31.13"),
    RAM_MEMORY_ERROR        (8, "0.18.85.79"),
    PROGRAM_FLOW_ERROR      (9, "0.11.83.79"),
    REGISTER_OVERFLOW       (10, "0.21.89.177"),
    FATAL_ERROR             (11, "0.0.43.79"),
    CLEAR_DATA              (12, "0.18.31.28"),
    HARDWARE_ERROR          (13, "0.0.0.79"),
    METER_ALARM             (14, "0.11.46.79"),
    ROM_MEMORY_ERROR        (15, "0.18.92.79"),
    MAXIMUM_DEMAND_RESET    (16, "0.8.87.215"),
    BILLING_ACTION          (17, "0.20.43.44"),
    APPLICATION_ALERT_START (18, "0.11.43.242"),
    APPLICATION_ALERT_STOP  (19, "0.11.43.243"),
    PHASE_FAILURE           (20, "1.26.25.85"),
    VOLTAGE_SAG             (21, "1.26.79.223"),
    VOLTAGE_SWELL           (22, "1.26.79.248"),
    TAMPER                  (23, "0.12.43.257"),
    COVER_OPENED            (24, "0.12.29.39"),
    TERMINAL_OPENED         (25, "0.12.141.39"),
    REVERSE_RUN             (26, "0.12.48.219"),
    LOADPROFILE_CLEARED     (27, "0.16.87.28"),
    EVENT_LOG_CLEARED       (28, "0.17.44.28"),
    DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED(29, "0.36.56.24"),
    CLOCK_INVALID           (30, "0.36.43.35"),
    REPLACE_BATTERY         (31, "0.2.0.150"),
    BATTERY_VOLTAGE_LOW     (32, "0.2.22.150"),
    TOU_ACTIVATED           (33, "0.20.121.4"),
    ERROR_REGISTER_CLEARED  (34, "0.17.89.28"),
    ALARM_REGISTER_CLEARED  (35, "0.17.89.28"),
    PROGRAM_MEMORY_ERROR    (36, "0.18.83.79"),
    NV_MEMORY_ERROR         (37, "0.18.72.79"),
    WATCHDOG_ERROR          (38, "0.11.3.79"),
    MEASUREMENT_SYSTEM_ERROR(39, "0.21.67.79"),
    FIRMWARE_READY_FOR_ACTIVATION(40, "0.11.31.25"),
    FIRMWARE_ACTIVATED      (41, "0.11.31.4"),
    TERMINAL_COVER_CLOSED   (42, "0.12.141.16"),
    STRONG_DC_FIELD_DETECTED(43, "0.12.66.242"),
    NO_STRONG_DC_FIELD_ANYMORE(44, "0.12.66.243"),
    METER_COVER_CLOSED      (45, "0.12.29.16"),
    N_TIMES_WRONG_PASSWORD  (46, "0.12.24.7"),
    MANUAL_DISCONNECTION    (47, "0.31.0.68"),
    MANUAL_CONNECTION       (48, "0.31.0.42"),
    REMOTE_DISCONNECTION    (49, "0.31.0.68"),
    REMOTE_CONNECTION       (50, "0.31.0.42"),
    LOCAL_DISCONNECTION     (51, "0.31.0.68"),
    LIMITER_THRESHOLD_EXCEEDED(52, "0.15.261.139"),
    LIMITER_THRESHOLD_OK    (53, "0.15.261.216"),
    LIMITER_THRESHOLD_CHANGED(54, "0.15.261.24"),
    COMMUNICATION_ERROR_MBUS(55, "0.1.147.79"),
    COMMUNICATION_OK_MBUS   (56, "0.1.147.216"),
    REPLACE_BATTERY_MBUS    (57, "0.2.147.150"),
    FRAUD_ATTEMPT_MBUS      (58, "0.12.147.7"),
    CLOCK_ADJUSTED_MBUS     (59, "0.36.147.24"),
    MANUAL_DISCONNECTION_MBUS(60, "0.31.147.68"),
    MANUAL_CONNECTION_MBUS  (61, "0.31.147.42"),
    REMOTE_DISCONNECTION_MBUS(62, "0.31.147.68"),
    REMOTE_CONNECTION_MBUS  (63, "0.31.147.42"),
    VALVE_ALARM_MBUS        (64, "0.31.147.79");

    private final int eisCode;
    private final String obisCode;
    private EndDeviceEventType eventType;

    private EndDeviceEventTypeMapping(int eisCode, String obisCode) {
        this.eisCode = eisCode;
        this.obisCode = obisCode;
    }

    public int getEisCode() {
        return eisCode;
    }

    public EndDeviceEventType getEventType() {
        if (eventType == null) {
            eventType = EndDeviceEventTypeFactory.current.get().getEndDeviceEventType(obisCode);
        }
        return eventType;
    }

    public static EndDeviceEventType getEventTypeCorrespondingToEISCode(int eisCode) {
        for (EndDeviceEventTypeMapping each : EndDeviceEventTypeMapping.values()) {
            if (each.getEisCode() == eisCode) {
                return each.getEventType();
            }
        }
        return OTHER.getEventType();
    }

    public static int getEISCodeCorrespondingToEventType(EndDeviceEventType endDeviceEventType) {
        for (EndDeviceEventTypeMapping each : EndDeviceEventTypeMapping.values()) {
            if (each.getEventType().toString().equals(endDeviceEventType.toString())) {
                return each.getEisCode();
            }
        }
        return OTHER.getEisCode();
    }
}
