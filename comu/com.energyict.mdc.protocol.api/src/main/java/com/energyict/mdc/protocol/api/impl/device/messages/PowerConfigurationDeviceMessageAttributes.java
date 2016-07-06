package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

/**
 * Copyrights EnergyICT
 * Date: 30.04.15
 * Time: 15:35
 */
enum PowerConfigurationDeviceMessageAttributes implements TranslationKey {

    ReferenceVoltageAttributeName(DeviceMessageConstants.ReferenceVoltageAttributeName, "ReferenceVoltage"),
    VoltageSagTimeThresholdAttributeName(DeviceMessageConstants.VoltageSagTimeThresholdAttributeName, "VoltageSagTimeThreshold"),
    VoltageSwellTimeThresholdAttributeName(DeviceMessageConstants.VoltageSwellTimeThresholdAttributeName, "VoltageSwellTimeThreshold"),
    VoltageSagThresholdAttributeName(DeviceMessageConstants.VoltageSagThresholdAttributeName, "VoltageSagThreshold"),
    VoltageSwellThresholdAttributeName(DeviceMessageConstants.VoltageSwellThresholdAttributeName, "VoltageSwellThreshold"),
    LongPowerFailureTimeThresholdAttributeName(DeviceMessageConstants.LongPowerFailureTimeThresholdAttributeName, "LongPowerFailureTimeThreshold"),
    LongPowerFailureThresholdAttributeName(DeviceMessageConstants.LongPowerFailureThresholdAttributeName, "LongPowerFailureThreshold"),
    ;

    private final String key;
    private final String defaultFormat;

    PowerConfigurationDeviceMessageAttributes(String key, String defaultFormat) {
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