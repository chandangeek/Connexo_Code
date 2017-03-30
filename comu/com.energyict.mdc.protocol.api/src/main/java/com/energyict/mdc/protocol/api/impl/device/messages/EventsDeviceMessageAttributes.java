/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

enum EventsDeviceMessageAttributes implements TranslationKey {

    SetInputChannelAttributeName(DeviceMessageConstants.SetInputChannelAttributeName, "Set inputChannel"),
    SetConditionAttributeName(DeviceMessageConstants.SetConditionAttributeName, "Set condition"),
    SetConditionValueAttributeName(DeviceMessageConstants.SetConditionValueAttributeName, "Set conditionValue"),
    SetTimeTrueAttributeName(DeviceMessageConstants.SetTimeTrueAttributeName, "Set timeTrue"),
    SetTimeFalseAttributeName(DeviceMessageConstants.SetTimeFalseAttributeName, "Set timeFalse"),
    SetOutputChannelAttributeName(DeviceMessageConstants.SetOutputChannelAttributeName, "Set outputChannel"),
    SetAlarmAttributeName(DeviceMessageConstants.SetAlarmAttributeName, "Set alarm"),
    SetTagAttributeName(DeviceMessageConstants.SetTagAttributeName, "Set tag"),
    SetInverseAttributeName(DeviceMessageConstants.SetInverseAttributeName, "Set inverse"),
    SetImmediateAttributeName(DeviceMessageConstants.SetImmediateAttributeName, "Set immediate"),
    fromDateAttributeName("EventsConfigurationDeviceMessage." + DeviceMessageConstants.fromDateAttributeName, "from"),
    toDateAttributeName("EventsConfigurationDeviceMessage." + DeviceMessageConstants.toDateAttributeName, "to"),
    ;

    private final String key;
    private final String defaultFormat;

    EventsDeviceMessageAttributes(String key, String defaultFormat) {
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