/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-10 (10:08)
 */
public enum GarnetTranslationKeys implements TranslationKey {

    RETRIES("Garnet.dialect.retries", "Retries"),
    TIMEOUT("Garnet.dialect.timeout", "Timeout"),
    FORCED_DELAY("Garnet.dialect.forcedDelay", "Forced delay"),
    DELAY_AFTER_ERROR("Garnet.dialect.delayAfterError", "Delay after error"),
    ;

    private final String key;
    private final String defaultFormat;

    GarnetTranslationKeys(String key, String defaultFormat) {
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