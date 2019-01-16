/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.custompropertyset;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    DOMAIN_NAME_DEVICE("domain.name.device", "Device"),
    DOMAIN_NAME_CHANNEL("domain.name.channel", "Channel"),
    DOMAIN_NAME_REGISTER("domain.name.register", "Register"),
    CPS_DEVICE_SAP_INFO("device.cps.properties.devicesapinfo", "Device SAP info"),
    CPS_DEVICE_CHANNEL_SAP_INFO("device.cps.properties.devicechannelsapinfo", "Device channel SAP info"),
    CPS_DEVICE_REGISTER_SAP_INFO("device.cps.properties.deviceregistersapinfo", "Device register SAP info"),
    CPS_DEVICE_IDENTIFIER("device.cps.properties.deviceIdentifier", "Device identifier"),
    CPS_DEVICE_IDENTIFIER_DESCRIPTION("device.cps.properties.deviceIdentifier.description", "Device identifier in SAP."),
    CPS_LOGICAL_REGISTER_NUMBER("device.cps.properties.logicalNumberIdentifier", "Logical register number"),
    CPS_DEVICE_CHANNEL_IDENTIFIER_DESCRIPTION("device.cps.properties.deviceChannelIdentifier.description", "Device channel identifier in SAP."),
    CPS_DEVICE_REGISTER_IDENTIFIER_DESCRIPTION("device.cps.properties.deviceRegisterIdentifier.description", "Device register identifier in SAP."),

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