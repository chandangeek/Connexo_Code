/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

enum EIWebConfigurationDeviceMessageAttributes implements TranslationKey {

    Id("EIWeb." + DeviceMessageConstants.id, "ID"),
    SetEIWebPasswordAttributeName(DeviceMessageConstants.SetEIWebPasswordAttributeName, "Set EIWeb password"),
    SetEIWebPageAttributeName(DeviceMessageConstants.SetEIWebPageAttributeName, "Set EIWeb page"),
    SetEIWebFallbackPageAttributeName(DeviceMessageConstants.SetEIWebFallbackPageAttributeName, "Set EIWeb fallback page"),
    SetEIWebSendEveryAttributeName(DeviceMessageConstants.SetEIWebSendEveryAttributeName, "Set EIWeb send rvery"),
    SetEIWebCurrentIntervalAttributeName(DeviceMessageConstants.SetEIWebCurrentIntervalAttributeName, "Set EIWeb current interval"),
    SetEIWebDatabaseIDAttributeName(DeviceMessageConstants.SetEIWebDatabaseIDAttributeName, "Set EIWeb databaseID"),
    SetEIWebOptionsAttributeName(DeviceMessageConstants.SetEIWebOptionsAttributeName, "Set EIWeb options"),
    ;

    private final String key;
    private final String defaultFormat;

    EIWebConfigurationDeviceMessageAttributes(String key, String defaultFormat) {
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