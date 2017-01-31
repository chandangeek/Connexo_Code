/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.cim;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventType;

import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Enum containing the mapping between the {@link com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent}s
 * EIServer code and their corresponding CIM {@link EndDeviceEventType}.
 *
 * @author sva
 * @since 3/06/13 - 15:00
 */
public enum EndDeviceEventTypeMapping {

    OTHER                   ("0.0.0.0"),
    POWERDOWN               ("0.26.38.47"),
    POWERUP                 ("0.26.38.49"),
    WATCHDOGRESET           ("0.11.3.215"),
    SETCLOCK_BEFORE         ("0.36.114.24"),
    SETCLOCK_AFTER          ("0.36.114.24"),
    SETCLOCK                ("0.36.114.13"),
    CONFIGURATIONCHANGE     ("0.7.31.13"),
    RAM_MEMORY_ERROR        ("0.18.85.79"),
    PROGRAM_FLOW_ERROR      ("0.11.83.79"),
    REGISTER_OVERFLOW       ("0.21.89.177"),
    FATAL_ERROR             ("0.0.43.79"),
    CLEAR_DATA              ("0.18.31.28"),
    HARDWARE_ERROR          ("0.0.0.79"),
    METER_ALARM             ("0.11.46.79"),
    ROM_MEMORY_ERROR        ("0.18.92.79"),
    MAXIMUM_DEMAND_RESET    ("0.8.87.215"),
    BILLING_ACTION          ("0.20.43.44"),
    APPLICATION_ALERT_START ("0.11.43.242"),
    APPLICATION_ALERT_STOP  ("0.11.43.243"),
    PHASE_FAILURE           ("1.26.25.85"),
    VOLTAGE_SAG             ("1.26.79.223"),
    VOLTAGE_SWELL           ("1.26.79.248"),
    TAMPER                  ("0.12.43.257"),
    COVER_OPENED            ("0.12.29.39"),
    TERMINAL_OPENED         ("0.12.128.39"), // remapped from 0.12.141.39
    REVERSE_RUN             ("0.12.48.219"),
    LOADPROFILE_CLEARED     ("0.16.87.28"),
    EVENT_LOG_CLEARED       ("0.17.44.28"),
    DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED("0.36.56.24"),
    CLOCK_INVALID           ("0.36.43.35"),
    REPLACE_BATTERY         ("0.2.0.150"),
    BATTERY_VOLTAGE_LOW     ("0.2.22.150"),
    TOU_ACTIVATED           ("0.20.121.4"),
    ERROR_REGISTER_CLEARED  ("0.17.89.28"),
    ALARM_REGISTER_CLEARED  ("0.17.89.28"),
    PROGRAM_MEMORY_ERROR    ("0.18.83.79"),
    NV_MEMORY_ERROR         ("0.18.72.79"),
    WATCHDOG_ERROR          ("0.11.3.79"),
    MEASUREMENT_SYSTEM_ERROR("0.21.67.79"),
    FIRMWARE_READY_FOR_ACTIVATION("0.11.31.25"),
    FIRMWARE_ACTIVATED      ("0.11.31.4"),
    TERMINAL_COVER_CLOSED   ("0.12.128.16"), // remapped from 0.12.141.16
    STRONG_DC_FIELD_DETECTED("0.12.66.242"),
    NO_STRONG_DC_FIELD_ANYMORE("0.12.66.243"),
    METER_COVER_CLOSED      ("0.12.29.16"),
    N_TIMES_WRONG_PASSWORD  ("0.12.24.7"),
    MANUAL_DISCONNECTION    ("0.31.0.68"),
    MANUAL_CONNECTION       ("0.31.0.42"),
    REMOTE_DISCONNECTION    ("0.31.0.68"),
    REMOTE_CONNECTION       ("0.31.0.42"),
    LOCAL_DISCONNECTION     ("0.31.0.68"),
    LIMITER_THRESHOLD_EXCEEDED("0.15.261.139"),
    LIMITER_THRESHOLD_OK    ("0.15.261.216"),
    LIMITER_THRESHOLD_CHANGED("0.15.261.24"),
    COMMUNICATION_ERROR_MBUS("0.1.147.79"),
    COMMUNICATION_OK_MBUS   ("0.1.147.216"),
    REPLACE_BATTERY_MBUS    ("0.2.147.150"),
    FRAUD_ATTEMPT_MBUS      ("0.12.147.7"),
    CLOCK_ADJUSTED_MBUS     ("0.36.147.24"),
    MANUAL_DISCONNECTION_MBUS("0.31.147.68"),
    MANUAL_CONNECTION_MBUS  ("0.31.147.42"),
    REMOTE_DISCONNECTION_MBUS("0.31.147.68"),
    REMOTE_CONNECTION_MBUS  ("0.31.147.42"),
    VALVE_ALARM_MBUS        ("0.31.147.79");

    private static final Logger LOGGER = Logger.getLogger(EndDeviceEventTypeMapping.class.getName());

    private final String endDeviceEventTypeMRID;
    private EndDeviceEventType eventType;

    private EndDeviceEventTypeMapping(String endDeviceEventTypeMRID) {
        this.endDeviceEventTypeMRID = endDeviceEventTypeMRID;
    }

    public int getEisCode() {
        return this.ordinal();
    }

    public String getEndDeviceEventTypeMRID() {
        return endDeviceEventTypeMRID;
    }

    public Optional<EndDeviceEventType> getEventType(MeteringService meteringService) {
        if (eventType == null) {
            Optional<EndDeviceEventType> endDeviceEventType = meteringService.getEndDeviceEventType(this.endDeviceEventTypeMRID);
            if (endDeviceEventType.isPresent()) {
                eventType = endDeviceEventType.orElse(null);
            }
            else {
                LOGGER.severe(() -> "EndDeviceEventType missing: " + this.endDeviceEventTypeMRID);
            }
            return endDeviceEventType;
        }
        else {
            return Optional.of(this.eventType);
        }
    }

    public static Optional<EndDeviceEventType> getEventTypeCorrespondingToEISCode(int eisCode, MeteringService meteringService) {
        return Stream.of(EndDeviceEventTypeMapping.values())
                .filter(each -> each.getEisCode() == eisCode)
                .findFirst()
                .map(each -> each.getEventType(meteringService))
                .orElse(OTHER.getEventType(meteringService));
    }

}