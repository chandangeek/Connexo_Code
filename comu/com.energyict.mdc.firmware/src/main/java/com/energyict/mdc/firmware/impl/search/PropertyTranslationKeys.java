/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl.search;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.firmware.FirmwareType;

/**
 * Provides the translation keys for the search properties
 * that are supported by the device data bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-01 (15:46)
 */
public enum PropertyTranslationKeys implements TranslationKey {

    FIRMWARE(FirmwareSearchablePropertyGroup.GROUP_NAME, "Firmware"),
    FIRMWARE_VERSION(FirmwareVersionSearchableProperty.PROPERTY_NAME, "Firmware version"),
    FIRMWARE_TYPE(FirmwareTypeSearchableProperty.PROPERTY_NAME, "Firmware type"),
    FIRMWARE_TYPE_METER("firmware.type." + FirmwareType.METER.name(), "Device"),
    FIRMWARE_TYPE_COMMUNICATION("firmware.type." + FirmwareType.COMMUNICATION.name(), "Communication"),
    FIRMWARE_TYPE_CA_CONFIG_IMAGE("firmware.type." + FirmwareType.CA_CONFIG_IMAGE.name(), "Image"),
    FIRMWARE_TYPE_AUXILIARY("firmware.type." + FirmwareType.AUXILIARY.name(), "Auxiliary");

    private final String key;
    private final String defaultFormat;

    PropertyTranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }


    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

}
