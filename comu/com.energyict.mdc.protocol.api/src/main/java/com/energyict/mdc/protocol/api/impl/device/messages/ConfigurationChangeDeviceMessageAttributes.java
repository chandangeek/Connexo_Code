/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

enum ConfigurationChangeDeviceMessageAttributes implements TranslationKey {

    enableSSL(DeviceMessageConstants.enableSSL, "Enable SSL"),
    deviceName(DeviceMessageConstants.deviceName, "Device name"),
    ntpAddress(DeviceMessageConstants.ntpAddress, "NTP address"),
    enableAutomaticDemandResetAttributeName(DeviceMessageConstants.enableAutomaticDemandResetAttributeName, "Enable demand reset"),
    enableDSTAttributeName(DeviceMessageConstants.enableDSTAttributeName, "Enable daylight savings time"),
    localMacAddress(DeviceMessageConstants.localMacAddress, "Local Mac address"),
    maxCredit(DeviceMessageConstants.maxCredit, "Maximium credit"),
    zeroCrossDelay(DeviceMessageConstants.zeroCrossDelay, "Zero cross delay"),
    synchronisationBit(DeviceMessageConstants.synchronisationBit, "Synchronisation bit"),
    day("ConfigurationChangeDeviceMessage." + DeviceMessageConstants.day, "Day"),
    hour("ConfigurationChangeDeviceMessage." + DeviceMessageConstants.hour, "Hour"),
    MeterScheme(DeviceMessageConstants.MeterScheme, "Meter scheme"),
    SwitchPointClockSettings(DeviceMessageConstants.SwitchPointClockSettings, "Switchpoint clock settings"),
    SwitchPointClockUpdateSettings(DeviceMessageConstants.SwitchPointClockUpdateSettings, "Switchpoint clock update settings"),
    SetMBusEveryAttributeName(DeviceMessageConstants.SetMBusEveryAttributeName, "Set MBus every"),
    SetMBusInterFrameTimeAttributeName(DeviceMessageConstants.SetMBusInterFrameTimeAttributeName, "Set MBus inter frame time"),
    SetMBusConfigAttributeName(DeviceMessageConstants.SetMBusConfigAttributeName, "Set MBus config"),
    SetMBusVIFAttributeName(DeviceMessageConstants.SetMBusVIFAttributeName, "MBus VIF"),
    powerQualityThresholdAttributeName(DeviceMessageConstants.powerQualityThresholdAttributeName, "powerQualityThreshold"),
    ;

    private final String key;
    private final String defaultFormat;

    ConfigurationChangeDeviceMessageAttributes(String key, String defaultFormat) {
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