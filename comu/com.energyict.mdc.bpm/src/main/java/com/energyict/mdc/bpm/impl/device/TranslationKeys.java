/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.bpm.impl.device;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Created by dragos on 2/18/2016.
 */

public enum TranslationKeys implements TranslationKey {
    DEVICE_ASSOCIATION_PROVIDER(DeviceProcessAssociationProvider.ASSOCIATION_TYPE, "Device"),
    DEVICE_STATE_TITLE("deviceStates", "Device states"),
    DEVICE_LIFECYCLE_COLUMN("deviceLifecycle", "Device life cycle"),
    DEVICE_STATE_COLUMN("deviceState", "Device state");

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
