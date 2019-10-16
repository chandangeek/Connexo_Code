/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.custom.custompropertyset;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    DOMAIN_NAME_DEVICE("domain.name.device", "Device"),
    CPS_DEVICE_POWER_FACTOR("device.cps.properties.powerfactor", "Power factor"),
    CPS_DEVICE_SETPOINT_THRESHOLD("device.cps.properties.setpointthreshold", "Setpoint threshold"),
    CPS_DEVICE_SETPOINT_THRESHOLD_DESCRIPTION("device.cps.properties.setpointthreshold.description", "threshold for result value of the power factor"),
    CPS_DEVICE_HYSTERESIS_PERCENTAGE("device.cps.properties.hysteresispercentage", "Hysteresis percentage"),
    CPS_DEVICE_HYSTERESIS_PERCENTAGE_DESCRIPTION("device.cps.properties.hysteresispercentage.description", "allowable deviation from the threshold (in percent [0-100])"),
    CPS_DEVICE_FLAG("device.cps.properties.flag", "Flag"),
    CPS_DEVICE_FLAG_DESCRIPTION("device.cps.properties.flag.description", "flag"),
    CPS_DEVICE_MAX_DEMAND("device.cps.properties.maxdemand", "Max demand"),
    CPS_DEVICE_CONNECTED_LOAD("device.cps.properties.connectedload", "Connected load"),
    CPS_DEVICE_CONNECTED_LOAD_DESCRIPTION("device.cps.properties.connectedload.description", "Connected load"),
    CPS_DEVICE_UNIT("device.cps.properties.unit", "Unit"),
    CPS_DEVICE_UNIT_DESCRIPTION("device.cps.properties.unit.description", "unit of the connected load value kW/MW"),
    CPS_DEVICE_CT_RATIO("device.cps.properties.ctratio", "CT ratio"),
    CPS_DEVICE_CT_RATIO_DESCRIPTION("device.cps.properties.ctratio.description", "CT ratio"),
    ;

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }
}
