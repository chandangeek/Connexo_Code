/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Created by bvn on 6/15/16.
 */
public enum TranslationKeys implements TranslationKey {

    LL_SEVERE("webservices.loglevel.severe", "Severe"),
    LL_WARNING("webservices.loglevel.warning", "Warning"),
    LL_INFO("webservices.loglevel.info", "Information"),
    LL_CONFIG("webservices.loglevel.config", "Configuration"),
    LL_FINE("webservices.loglevel.fine", "Fine"),
    LL_FINER("webservices.loglevel.finer", "Finer"),
    LL_FINEST("webservices.loglevel.finest", "Finest"),
    WST_INBOUND("webservices.direction.inbound", "Inbound"),
    WST_OUTBOUND("webservices.direction.outbound", "Outbound"),
    AUTH_NONE("webservices.authentication.none", "No authentication"),
    AUTH_BASIC("webservices.authentication.basic_authentication", "Basic authentication");
    ;

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
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