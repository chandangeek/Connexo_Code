/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.cim;

import java.util.Objects;

/**
 * Enum containing the mapping between the meter protocol event code and the corresponding CIM {@link EndDeviceEventType}.
 *
 * @author sva
 * @since 3/06/13 - 15:00
 */
public enum EndDeviceEventTypeMapping {

    OTHER                   (0, EndDeviceEventTypeFactory.getOtherEventType()),
    POWERDOWN               (1, EndDeviceEventTypeFactory. getPowerDownEventType()),
    POWERUP                 (2, EndDeviceEventTypeFactory.getPowerUpEventType()),
    WATCHDOGRESET           (3, EndDeviceEventTypeFactory.getWatchdogResetEventType()),
    SETCLOCK_BEFORE         (4, EndDeviceEventTypeFactory.getSetClockBeforeEventType()),
    SETCLOCK_AFTER          (5, EndDeviceEventTypeFactory.getSetClockAfterEventType()),
    SETCLOCK                (6, EndDeviceEventTypeFactory.getSetClockEventType()),
    CONFIGURATIONCHANGE     (7, EndDeviceEventTypeFactory.getConfigurationChangeEventType()),
    RAM_MEMORY_ERROR        (8, EndDeviceEventTypeFactory.getRamMemoryErrorEventType()),
    PROGRAM_FLOW_ERROR      (9, EndDeviceEventTypeFactory.getProgramFlowErrorEventType()),
    REGISTER_OVERFLOW       (10, EndDeviceEventTypeFactory.getRegisterOverflowEventType()),
    FATAL_ERROR             (11, EndDeviceEventTypeFactory.getFatalErrorEventType()),
    CLEAR_DATA              (12, EndDeviceEventTypeFactory.getClearDataEventType()),
    HARDWARE_ERROR          (13, EndDeviceEventTypeFactory.getHardwareErrorEventType()),
    METER_ALARM             (14, EndDeviceEventTypeFactory.getMeterAlarmEventType()),
    ROM_MEMORY_ERROR        (15, EndDeviceEventTypeFactory.getRomMemoryErrorEventType()),
    MAXIMUM_DEMAND_RESET    (16, EndDeviceEventTypeFactory.getMaximumDemandResetEventType()),
    BILLING_ACTION          (17, EndDeviceEventTypeFactory.getBillingActionEventType()),
    APPLICATION_ALERT_START (18, EndDeviceEventTypeFactory.getApplicationAlertStartEventType()),
    APPLICATION_ALERT_STOP  (19, EndDeviceEventTypeFactory.getApplicationAlertStopEventType()),
    PHASE_FAILURE           (20, EndDeviceEventTypeFactory.getPhaseFailureEventType()),
    VOLTAGE_SAG             (21, EndDeviceEventTypeFactory.getVoltageSagEventType()),
    VOLTAGE_SWELL           (22, EndDeviceEventTypeFactory.getVoltageSwellEventType()),
    TAMPER                  (23, EndDeviceEventTypeFactory.getTamperEventType()),
    COVER_OPENED            (24, EndDeviceEventTypeFactory.getCoverOpenedEventType()),
    TERMINAL_OPENED         (25, EndDeviceEventTypeFactory.getTerminalOpenedEventType()),
    REVERSE_RUN             (26, EndDeviceEventTypeFactory.getReverseRunEventType()),
    LOADPROFILE_CLEARED     (27, EndDeviceEventTypeFactory.getLoadProfileClearedEventType()),
    EVENT_LOG_CLEARED       (28, EndDeviceEventTypeFactory.getEventLogClearedEventType()),
    DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED(29,EndDeviceEventTypeFactory.getDaylightSavingTimeEnabledOrDisabledEventType()),
    CLOCK_INVALID           (30, EndDeviceEventTypeFactory.getClockInvalidEventType()),
    REPLACE_BATTERY         (31, EndDeviceEventTypeFactory.getReplaceBatteryEventType()),
    BATTERY_VOLTAGE_LOW     (32, EndDeviceEventTypeFactory.getBatteryVoltageLowEventType()),
    TOU_ACTIVATED           (33, EndDeviceEventTypeFactory.getTouActivatedEventType()),
    ERROR_REGISTER_CLEARED  (34, EndDeviceEventTypeFactory.getErrorRegisterClearedEventType()),
    ALARM_REGISTER_CLEARED  (35, EndDeviceEventTypeFactory.getAlarmRegisterClearedEventType()),
    PROGRAM_MEMORY_ERROR    (36, EndDeviceEventTypeFactory.getProgramMemoryErrorEventType()),
    NV_MEMORY_ERROR         (37, EndDeviceEventTypeFactory.getNvMemoryErrorEventType()),
    WATCHDOG_ERROR          (38, EndDeviceEventTypeFactory.getWatchdogErrorEventType()),
    MEASUREMENT_SYSTEM_ERROR(39, EndDeviceEventTypeFactory.getMeasurementSystemErrorEventType()),
    FIRMWARE_READY_FOR_ACTIVATION(40, EndDeviceEventTypeFactory.getFirmwareReadyForActivationEventType()),
    FIRMWARE_ACTIVATED      (41, EndDeviceEventTypeFactory.getFirmwareActivatedEventType()),
    TERMINAL_COVER_CLOSED   (42, EndDeviceEventTypeFactory.getTerminalCoverClosedEventType()),
    STRONG_DC_FIELD_DETECTED(43, EndDeviceEventTypeFactory.getStrongDCFieldDetectedEventType()),
    NO_STRONG_DC_FIELD_ANYMORE(44, EndDeviceEventTypeFactory.getNoStrongDCFieldAnymoreEventType()),
    METER_COVER_CLOSED      (45, EndDeviceEventTypeFactory.getMeterCoverClosedEventType()),
    N_TIMES_WRONG_PASSWORD  (46, EndDeviceEventTypeFactory.getNTimesWrongPasswordEventType()),
    MANUAL_DISCONNECTION    (47, EndDeviceEventTypeFactory.getManualDisconnectionEventType()),
    MANUAL_CONNECTION       (48, EndDeviceEventTypeFactory.getManualConnectionEventType()),
    REMOTE_DISCONNECTION    (49, EndDeviceEventTypeFactory.getRemoteDisconnectionEventType()),
    REMOTE_CONNECTION       (50, EndDeviceEventTypeFactory.getRemoteConnectionEventType()),
    LOCAL_DISCONNECTION     (51, EndDeviceEventTypeFactory.getLocalDisconnectionEventType()),
    LIMITER_THRESHOLD_EXCEEDED(52, EndDeviceEventTypeFactory.getLimiterThresholdExceededEventType()),
    LIMITER_THRESHOLD_OK    (53, EndDeviceEventTypeFactory.getLimiterThresholdOkEventType()),
    LIMITER_THRESHOLD_CHANGED(54, EndDeviceEventTypeFactory.getLimiterThresholdChangedEventType()),
    COMMUNICATION_ERROR_MBUS(55, EndDeviceEventTypeFactory.getCommunicationErrorMbusEventType()),
    COMMUNICATION_OK_MBUS   (56, EndDeviceEventTypeFactory.getCommunicationOkMbusEventType()),
    REPLACE_BATTERY_MBUS    (57, EndDeviceEventTypeFactory.getReplaceBatteryMbusEventType()),
    FRAUD_ATTEMPT_MBUS      (58, EndDeviceEventTypeFactory.getFraudAttemptMbusEventType()),
    CLOCK_ADJUSTED_MBUS     (59, EndDeviceEventTypeFactory.getClockAdjustedMbusEventType()),
    MANUAL_DISCONNECTION_MBUS(60, EndDeviceEventTypeFactory.getManualDisconnectionMbusEventType()),
    MANUAL_CONNECTION_MBUS  (61, EndDeviceEventTypeFactory.getManualConnectionMbusEventType()),
    REMOTE_DISCONNECTION_MBUS(62, EndDeviceEventTypeFactory.getRemoteDisconnectionMbusEventType()),
    REMOTE_CONNECTION_MBUS  (63, EndDeviceEventTypeFactory.getRemoteConnectionMbusEventType()),
    VALVE_ALARM_MBUS        (64, EndDeviceEventTypeFactory.getValveAlarmMbusEventType()),
    TAMPER_CLEARED          (65, EndDeviceEventTypeFactory.getTamperClearedEventType());

    private final int eisCode;
    private final EndDeviceEventType eventType;

    private EndDeviceEventTypeMapping(int eisCode, EndDeviceEventType eventType) {
        this.eisCode = eisCode;
        this.eventType = eventType;
    }

    public int getEisCode() {
        return eisCode;
    }

    public EndDeviceEventType getEventType() {
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
            if (Objects.equals(each.getEventType().toString(), endDeviceEventType.toString())) {
                return each.getEisCode();
            }
        }
        return OTHER.getEisCode();
    }
}
