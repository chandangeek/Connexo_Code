/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.impl.servicecall;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.servicecall.ServiceCall;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-28 (15:51)
 */
public enum TranslationKeys implements TranslationKey {
    CPS_DOMAIN_NAME_SERVICE_CALL(ServiceCall.class.getName(), "Service call");

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