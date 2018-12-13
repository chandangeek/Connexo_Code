/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edmi.mk6.profiles;

import com.energyict.obis.ObisCode;

/**
 * Created by sva on 10/03/2017.
 */
public enum LoadProfileChannelDescription {

    IMPORT_WH_TOTAL("Import Wh Total", ObisCode.fromString("1.1.1.8.0.255")),
    EXPORT_WH_TOTAL("Export Wh Total", ObisCode.fromString("1.1.2.8.0.255")),
    IMPORT_VARH_TOTAL("Import VArh Total", ObisCode.fromString("1.1.3.8.0.255")),
    EXPORT_VARH_TOTAL("Export VArh Total", ObisCode.fromString("1.1.4.8.0.255")),

    MIN_VOLTAGE_PHASE_A("Min Voltage Phase A", ObisCode.fromString("1.1.32.3.0.255")),
    MIN_VOLTAGE_PHASE_B("Min Voltage Phase B", ObisCode.fromString("1.1.52.3.0.255")),
    MIN_VOLTAGE_PHASE_C("Min Voltage Phase C", ObisCode.fromString("1.1.72.3.0.255")),
    MAX_VOLTAGE_PHASE_A("Max Voltage Phase A", ObisCode.fromString("1.1.32.6.0.255")),
    MAX_VOLTAGE_PHASE_B("Max Voltage Phase B", ObisCode.fromString("1.1.52.6.0.255")),
    MAX_VOLTAGE_PHASE_C("Max Voltage Phase C", ObisCode.fromString("1.1.72.6.0.255")),
    AVG_CURRENT_PHASE_A("Avg Current Phase A", ObisCode.fromString("1.1.31.4.0.255")),
    AVG_CURRENT_PHASE_B("Avg Current Phase B", ObisCode.fromString("1.1.51.4.0.255")),
    AVG_CURRENT_PHASE_C("Avg Current Phase C", ObisCode.fromString("1.1.71.4.0.255")),

    PULSING_INPUT_1("Pulsing Input 1", ObisCode.fromString("1.1.82.8.0.255")),
    PULSING_INPUT_2("Pulsing Input 2", ObisCode.fromString("1.1.82.8.0.255")),;

    private final String name;
    private final ObisCode obisCode;

    LoadProfileChannelDescription(String name, ObisCode obisCode) {
        this.name = name;
        this.obisCode = obisCode;
    }

    public String getName() {
        return name;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public static LoadProfileChannelDescription channelDescriptionForName(String name) {
        for (LoadProfileChannelDescription loadProfileChannelDescription : values()) {
            if (name.toLowerCase().contains(loadProfileChannelDescription.getName().toLowerCase())) {
                return loadProfileChannelDescription;
            }
        }
        return null;
    }
}