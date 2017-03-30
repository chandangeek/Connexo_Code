/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.common;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Common protocol V2 translations
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-10 (10:08)
 */
public enum CommonV2TranslationKeys implements TranslationKey {

    RETRIES("common.retries", "Retries"),
    TIMEZONE("common.timezone", "Timezone"),
    TIMEOUT("common.timeout", "Timeout"),
    FORCED_DELAY("common.forcedDelay", "Forced delay"),
    DELAY_AFTER_ERROR("common.delayAfterError", "Delay after error"),
    ROUNDTRIP_CORRECTION("common.roundTripCorrection", "Roundtrip correction"),
    INFORMATION_FIELD_SIZE("common.informationFieldSize", "Information field size"),
    ADDRESSING_MODE("common.addressingMode", "Addressing mode");

    private final String key;
    private final String defaultFormat;

    CommonV2TranslationKeys(String key, String defaultFormat) {
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