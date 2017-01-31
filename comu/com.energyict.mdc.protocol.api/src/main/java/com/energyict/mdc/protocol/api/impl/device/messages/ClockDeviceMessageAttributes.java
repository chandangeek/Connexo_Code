/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

enum ClockDeviceMessageAttributes implements TranslationKey {

    meterTimeAttributeName(DeviceMessageConstants.meterTimeAttributeName, "Meter time"),
    dstStartAlgorithmAttributeName(DeviceMessageConstants.dstStartAlgorithmAttributeName, "Daylight savings time start algorithm"),
    dstEndAlgorithmAttributeName(DeviceMessageConstants.dstEndAlgorithmAttributeName, "Daylight savings time end algorithm"),
    SetDSTAttributeName(DeviceMessageConstants.SetDSTAttributeName, "Set daylight savings time"),
    SetTimezoneAttributeName(DeviceMessageConstants.SetTimezoneAttributeName, "Set timezone"),
    TimeZoneOffsetInHoursAttributeName(DeviceMessageConstants.TimeZoneOffsetInHoursAttributeName, "GMT offset (hours)"),
    SetTimeAdjustmentAttributeName(DeviceMessageConstants.SetTimeAdjustmentAttributeName, "Set time adjustment"),
    SetNTPServerAttributeName(DeviceMessageConstants.SetNTPServerAttributeName, "Set NTP server"),
    SetRefreshClockEveryAttributeName(DeviceMessageConstants.SetRefreshClockEveryAttributeName, "Set refresh clock every"),
    SetNTPOptionsAttributeName(DeviceMessageConstants.SetNTPOptionsAttributeName, "Set NTP options"),
    StartOfDSTAttributeName(DeviceMessageConstants.StartOfDSTAttributeName, "Start of daylight savings time"),
    EndOfDSTAttributeName(DeviceMessageConstants.EndOfDSTAttributeName, "End of daylight savings time"),
    ;

    private final String key;
    private final String defaultFormat;

    ClockDeviceMessageAttributes(String key, String defaultFormat) {
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