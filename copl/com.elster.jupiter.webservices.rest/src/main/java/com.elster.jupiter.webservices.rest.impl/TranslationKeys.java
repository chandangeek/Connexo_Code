/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Created by bvn on 6/15/16.
 */
public enum TranslationKeys implements TranslationKey {
    WST_INBOUND("webservices.direction.inbound", "Inbound"),
    WST_OUTBOUND("webservices.direction.outbound", "Outbound"),
    AUTH_NONE("webservices.authentication.none", "No authentication"),
    AUTH_BASIC("webservices.authentication.basic_authentication", "Basic authentication"),
    AUTH_OAUTH2("webservices.authentication.oauth2_framework", "OAuth 2.0 Framework"),
    NAME_UNSPECIFIED("webservice.application.name.unspecified", "Unspecified");

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
