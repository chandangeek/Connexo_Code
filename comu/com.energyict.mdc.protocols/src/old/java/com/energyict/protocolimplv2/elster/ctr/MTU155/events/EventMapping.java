/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.Date;

public enum EventMapping {

    GENERIC(0x30, MeterEvent.OTHER, "Generic"),
    OVER_LIMIT(0x31, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Over Limit"),
    OUT_OF_RANGE(0x32, MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Out of range"),
    PROGRAMMING(0x33, MeterEvent.OTHER, "Programming"),
    MODIF_REL_PARAM(0x34, MeterEvent.CONFIGURATIONCHANGE, "Modification of a relevant parameter"),
    GENERAL_FAULT(0x35, MeterEvent.OTHER, "General fault"),
    PRIM_SUPPLY_OFF(0x36, MeterEvent.POWERDOWN, "Primary supply OFF"),
    BAT_LOW(0x37, MeterEvent.BATTERY_VOLTAGE_LOW, "Battery low"),
    MOD_DATE_TIME(0x38, MeterEvent.SETCLOCK_AFTER, "Modify date & time"),
    CALC_ERR(0x3A, MeterEvent.OTHER, "Calculation error"),
    MEM_RESET(0x3B, MeterEvent.CLEAR_DATA, "Memories reset"),
    REL_SEAL_DEACT(0x3C, MeterEvent.OTHER, "Relevant seal deactivated"),
    SYNC_ERR(0x3D, MeterEvent.OTHER, "Synchronization error"),
    RST_EVENT_QUEUE(0x3E, MeterEvent.EVENT_LOG_CLEARED, "Reset event queue"),
    DST_PROG(0x3F, MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED, "Day light saving time programming"),
    EVENT_BUFF_FULL(0x40, MeterEvent.OTHER, "Event buffer full"),
    TARIFF_CONF(0x41, MeterEvent.CONFIGURATIONCHANGE, "Tariff scheme configuration"),
    TARIFF_ACTIV(0x42, MeterEvent.OTHER, "Activation of a new tariff scheme"),
    DOWNL_FW(0x43, MeterEvent.FIRMWARE_READY_FOR_ACTIVATION, "Download of new software"),
    ACTIV_FW(0x44, MeterEvent.FIRMWARE_ACTIVATED, "Activation of new software"),
    FRAUD(0x46, MeterEvent.TAMPER, "Fraud attempt"),
    CHNG_STAT(0x47, MeterEvent.OTHER, "Change of status"),
    PROF_FAILED(0x48, MeterEvent.OTHER, "Programming failed"),
    FLOW_CO(0x49, MeterEvent.OTHER, "Flow cut-off"),
    PRESS_CO(0x4A, MeterEvent.OTHER, "Pressure cut-off"),
    HLT_CALC(0x4B, MeterEvent.OTHER, "Halt volume calculation at standard therm. cond."),
    MOD_OF_SEC_PARAM(0x4C, MeterEvent.COMMUNICATION_ERROR_MBUS, "Modification of security parameters"),
    REPLACE_BATT(0x4D, MeterEvent.REPLACE_BATTERY, "Replace batteries"),
    INSTALLATION_ARCHIVE(0x82, MeterEvent.OTHER, "Installation archive snapshot"),
    UNKNOWN(0x00, MeterEvent.OTHER, "Unknown event");

    private final int eisCode;
    private final int deviceCode;
    private final String description;

    private EventMapping(int deviceCode, int eisCode, String description) {
        this.description = description;
        this.deviceCode = deviceCode;
        this.eisCode = eisCode;
    }

    public String getDescription() {
        return description;
    }

    public int getDeviceCode() {
        return deviceCode;
    }

    public int getEisCode() {
        return eisCode;
    }

    public static MeterEvent getMeterEventFromDeviceCode(int deviceCode, Date eventDate) {
        EventMapping[] mappings = EventMapping.values();
        for (EventMapping mapping : mappings) {
            if (mapping.getDeviceCode() == deviceCode) {
                return new MeterEvent(eventDate, mapping.getEisCode(), deviceCode, mapping.getDescription());
            }
        }
        MeterEvent meterEvent = getMeterEventFromDeviceCode(0, eventDate);
        return new MeterEvent(eventDate, meterEvent.getEiCode(), deviceCode, meterEvent.getMessage());
    }

}
