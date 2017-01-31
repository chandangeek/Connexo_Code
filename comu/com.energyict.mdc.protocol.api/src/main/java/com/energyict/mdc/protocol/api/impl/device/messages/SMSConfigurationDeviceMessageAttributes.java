/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

enum SMSConfigurationDeviceMessageAttributes implements TranslationKey {

    SetSmsDataNbrAttributeName(DeviceMessageConstants.SetSmsDataNbrAttributeName, "Set Sms dataNbr"),
    SetSmsAlarmNbrAttributeName(DeviceMessageConstants.SetSmsAlarmNbrAttributeName, "Set Sms alarmNbr"),
    SetSmsEveryAttributeName(DeviceMessageConstants.SetSmsEveryAttributeName, "Set Sms every"),
    SetSmsNbrAttributeName(DeviceMessageConstants.SetSmsNbrAttributeName, "Set Sms nbr"),
    SetSmsCorrectionAttributeName(DeviceMessageConstants.SetSmsCorrectionAttributeName, "Set Sms correction"),
    SetSmsConfigAttributeName(DeviceMessageConstants.SetSmsConfigAttributeName, "Set Sms config"),
    ;

    private final String key;
    private final String defaultFormat;

    SMSConfigurationDeviceMessageAttributes(String key, String defaultFormat) {
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