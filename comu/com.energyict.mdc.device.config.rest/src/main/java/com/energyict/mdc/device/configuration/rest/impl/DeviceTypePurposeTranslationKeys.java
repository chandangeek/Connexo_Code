/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.config.DeviceTypePurpose;

public enum DeviceTypePurposeTranslationKeys implements TranslationKey {

    REGULAR(DeviceTypePurpose.REGULAR, "Standard device type"),
    DATALOGGER_SLAVE(DeviceTypePurpose.DATALOGGER_SLAVE, "Datalogger slave device type"),;

    private final DeviceTypePurpose deviceTypePurpose;
    private final String defaultFormat;

    DeviceTypePurposeTranslationKeys(DeviceTypePurpose deviceTypePurpose, String defaultFormat) {
        this.deviceTypePurpose = deviceTypePurpose;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.deviceTypePurpose.name();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }
}
