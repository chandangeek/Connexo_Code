/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.config.ConnectionStrategy;

public enum TranslationKeys implements TranslationKey {
    STATUS_COMPLETED("completed", "Completed"),
    STATUS_CONFIGURATION_ERROR("configurationError", "Configuration error"),
    FIRMWARE_COMTASK_NAME("firmwareComTaskName","Firmware management"),
    MINIMIZE_CONNECTIONS(ConnectionStrategy.MINIMIZE_CONNECTIONS.name(), "Minimize connections"),
    AS_SOON_AS_POSSIBLE(ConnectionStrategy.AS_SOON_AS_POSSIBLE.name(), "As soon as possible"),
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