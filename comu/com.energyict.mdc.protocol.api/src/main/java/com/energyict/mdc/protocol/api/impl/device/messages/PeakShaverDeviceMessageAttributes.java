/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

enum PeakShaverDeviceMessageAttributes implements TranslationKey {

    id("PeakShaverConfigurationDeviceMessage." + DeviceMessageConstants.loadIdAttributeName, "loadId"),
    loadIdAttributeName(DeviceMessageConstants.loadIdAttributeName, "loadId"),
    MaxOffAttributeName(DeviceMessageConstants.MaxOffAttributeName, "MaxOff"),
    DelayAttributeName(DeviceMessageConstants.DelayAttributeName, "Delay"),
    ManualAttributeName(DeviceMessageConstants.ManualAttributeName, "Manual"),
    StatusAttributeName(DeviceMessageConstants.StatusAttributeName, "Status"),
    PeakShaverIPAddressAttributeName(DeviceMessageConstants.PeakShaverIPAddressAttributeName, "IPAddress"),
    PeakShaveChnNbrAttributeName(DeviceMessageConstants.PeakShaveChnNbrAttributeName, "ChannelNumber"),
    SetActiveChannelAttributeName(DeviceMessageConstants.SetActiveChannelAttributeName, "SetActiveChannel"),
    SetReactiveChannelAttributeName(DeviceMessageConstants.SetReactiveChannelAttributeName, "SetReactiveChannel"),
    SetTimeBaseAttributeName(DeviceMessageConstants.SetTimeBaseAttributeName, "SetTimeBase"),
    SetPOutAttributeName(DeviceMessageConstants.SetPOutAttributeName, "SetPOut"),
    SetPInAttributeName(DeviceMessageConstants.SetPInAttributeName, "SetPIn"),
    SetDeadTimeAttributeName(DeviceMessageConstants.SetDeadTimeAttributeName, "SetDeadTime"),
    SetAutomaticAttributeName(DeviceMessageConstants.SetAutomaticAttributeName, "SetAutomatic"),
    SetCyclicAttributeName(DeviceMessageConstants.SetCyclicAttributeName, "SetCyclic"),
    SetInvertAttributeName(DeviceMessageConstants.SetInvertAttributeName, "SetInvert"),
    SetAdaptSetpointAttributeName(DeviceMessageConstants.SetAdaptSetpointAttributeName, "SetAdaptSetpoint"),
    SetInstantAnalogOutAttributeName(DeviceMessageConstants.SetInstantAnalogOutAttributeName, "SetInstantAnalogOut"),
    SetPredictedAnalogOutAttributeName(DeviceMessageConstants.SetPredictedAnalogOutAttributeName, "SetPredictedAnalogOut"),
    SetpointAnalogOutAttributeName(DeviceMessageConstants.SetpointAnalogOutAttributeName, "SetpointAnalogOut"),
    SetDifferenceAnalogOutAttributeName(DeviceMessageConstants.SetDifferenceAnalogOutAttributeName, "SetDifferenceAnalogOut"),
    SetTariffAttributeName(DeviceMessageConstants.SetTariffAttributeName, "SetTariff"),
    SetResetLoadsAttributeName(DeviceMessageConstants.SetResetLoadsAttributeName, "SetResetLoads"),
    CurrentValueAttributeName(DeviceMessageConstants.CurrentValueAttributeName, "CurrentValue"),
    NewValueAttributeName(DeviceMessageConstants.NewValueAttributeName, "NewValue"),
    tariff("PeakShaverConfigurationDeviceMessage." + DeviceMessageConstants.tariff, "tariff"),
    month("PeakShaverConfigurationDeviceMessage." + DeviceMessageConstants.month, "month"),
    year("PeakShaverConfigurationDeviceMessage." + DeviceMessageConstants.year, "year"),
    dayOfMonth("PeakShaverConfigurationDeviceMessage." + DeviceMessageConstants.dayOfMonth, "dayOfMonth"),
    day("PeakShaverConfigurationDeviceMessage." + DeviceMessageConstants.day, "day"),
    dayOfWeek("PeakShaverConfigurationDeviceMessage." + DeviceMessageConstants.dayOfWeek, "dayOfWeek"),
    hour("PeakShaverConfigurationDeviceMessage." + DeviceMessageConstants.hour, "hour"),
    minute("PeakShaverConfigurationDeviceMessage." + DeviceMessageConstants.minute, "minute"),
    second("PeakShaverConfigurationDeviceMessage." + DeviceMessageConstants.second, "second"),
    ;

    private final String key;
    private final String defaultFormat;

    PeakShaverDeviceMessageAttributes(String key, String defaultFormat) {
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