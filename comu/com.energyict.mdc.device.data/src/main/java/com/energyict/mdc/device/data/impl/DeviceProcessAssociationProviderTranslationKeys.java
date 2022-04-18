/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum DeviceProcessAssociationProviderTranslationKeys implements TranslationKey {
    DEVICE_ASSOCIATION_PROVIDER(DeviceProcessAssociationProvider.ASSOCIATION_TYPE, "Device"),
    DEVICE_STATE_TITLE("deviceStates", "Device states"),
    DEVICE_LIFECYCLE_COLUMN("deviceLifecycle", "Device life cycle"),
    DEVICE_STATE_COLUMN("deviceState", "Device state");

    private final String key;
    private final String defaultFormat;

    DeviceProcessAssociationProviderTranslationKeys(String key, String defaultFormat) {
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
