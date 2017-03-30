/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

enum ZigBeeConfigurationDeviceMessageAttributes implements TranslationKey {

    ZigBeeConfigurationSASPanIdAttributeName(DeviceMessageConstants.ZigBeeConfigurationSASPanIdAttributeName, "SAS PanId"),
    ZigBeeConfigurationForceRemovalAttributeName(DeviceMessageConstants.ZigBeeConfigurationForceRemovalAttributeName, "Force removal"),
    ZigBeeConfigurationZigBeeLinkKeyAttributeName(DeviceMessageConstants.ZigBeeConfigurationZigBeeLinkKeyAttributeName, "ZigBee link key"),
    ZigBeeConfigurationActivationDateAttributeName(DeviceMessageConstants.ZigBeeConfigurationActivationDateAttributeName, "Activation date"),
    ZigBeeConfigurationZigBeeAddressAttributeName(DeviceMessageConstants.ZigBeeConfigurationZigBeeAddressAttributeName, "ZigBee IEEE address"),
    ZigBeeConfigurationMirrorAddressAttributeName(DeviceMessageConstants.ZigBeeConfigurationMirrorAddressAttributeName, "Mirror IEEE address"),
    ZigBeeConfigurationFirmwareUpdateUserFileAttributeName(DeviceMessageConstants.ZigBeeConfigurationFirmwareUpdateUserFileAttributeName, "File"),
    ZigBeeConfigurationSASInsecureJoinAttributeName(DeviceMessageConstants.ZigBeeConfigurationSASInsecureJoinAttributeName, "SAS insecure join"),
    ZigBeeConfigurationSASExtendedPanIdAttributeName(DeviceMessageConstants.ZigBeeConfigurationSASExtendedPanIdAttributeName, "SAS extended PanId"),
    ZigBeeConfigurationSASPanChannelMaskAttributeName(DeviceMessageConstants.ZigBeeConfigurationSASPanChannelMaskAttributeName, "SAS Pan channel mask"),
    ZigBeeConfigurationHANRestoreUserFileAttributeName(DeviceMessageConstants.ZigBeeConfigurationHANRestoreUserFileAttributeName, "HAN restore file"),
    ;

    private final String key;
    private final String defaultFormat;

    ZigBeeConfigurationDeviceMessageAttributes(String key, String defaultFormat) {
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