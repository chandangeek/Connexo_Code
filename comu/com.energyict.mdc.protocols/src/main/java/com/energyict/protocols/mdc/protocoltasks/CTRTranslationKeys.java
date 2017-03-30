/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.protocoltasks;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-10 (10:08)
 */
public enum CTRTranslationKeys implements TranslationKey {

    RETRIES("CTR.retries", "Retries"),
    TIMEOUT("CTR.timeout", "Timeout"),
    FORCED_DELAY("CTR.forcedDelay", "Forced delay"),
    DELAY_AFTER_ERROR("CTR.delayAfterError", "Delay after error"),
    MAX_ALLOWED_INVALID_PROFILE_RESPONSES("CTR.max.allowed.invalid.profile.responses", "Maximum allowed invalid profile responses"),
    ADDRESS("CTR.address", "Address"),
    SEND_END_OF_SESSION("CTR.send.end.of.session", "Send end of session");

    private final String key;
    private final String defaultFormat;

    CTRTranslationKeys(String key, String defaultFormat) {
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