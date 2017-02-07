/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-03 (11:31)
 */
public enum TranslationKeys implements TranslationKey {

    DEVICE_TYPE("deviceType.with.article", "a device type"),
    CHANNEL("com.energyict.mdc.device.config.ChannelSpec", "Channel"),
    REGISTER("com.energyict.mdc.device.config.RegisterSpec", "Register"),
    NO_ENCRYPTION("noEncryption", "No encryption"),
    NO_AUTHENTICATION("noAuthentication", "No authentication"),
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