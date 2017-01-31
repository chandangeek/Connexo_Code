/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.TranslationKey;

public enum DeviceDataStatusContainer {
    ACTIVE("active", PropertyTranslationKeys.DEVICE_DATA_STATUS_ACTIVE),
    INACTIVE("inactive", PropertyTranslationKeys.DEVICE_DATA_STATUS_INACTIVE);

    private String id;
    private TranslationKey translationKey;

    DeviceDataStatusContainer(String id, TranslationKey translationKey) {
        this.id = id;
        this.translationKey = translationKey;
    }

    public String getId() {
        return this.id;
    }

    public TranslationKey getTranslation() {
        return this.translationKey;
    }
}
