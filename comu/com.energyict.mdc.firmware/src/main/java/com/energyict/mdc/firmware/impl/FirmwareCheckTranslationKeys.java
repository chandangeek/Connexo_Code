/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum FirmwareCheckTranslationKeys implements TranslationKey {
    MINIMUM_LEVEL_FIRMWARE("MinimumLevelFirmware", "Minimum level firmware"),
    NO_DOWNGRADE("NoDowngrade", "The target firmware version should have a higher rank than the current firmware version on the device"),
    MASTER_HAS_LATEST_FIRMWARE("MasterHasLatestFirmware", "Master has latest firmware"),
    NO_GHOST_FIRMWARE("NoGhostFirmware", "No ghost firmware on device");

    private String key;
    private String name;

    FirmwareCheckTranslationKeys(String key, String name) {
        this.key = key;
        this.name = name;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return name;
    }
}
