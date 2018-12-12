/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

/**
 * Created by bvn on 6/15/16.
 */
public enum EndPointAuthentication implements TranslationKey {
    NONE("None"),
    BASIC_AUTHENTICATION("Basic");

    private String name;

    EndPointAuthentication(String name) {
        this.name = name;
    }

    public String getDisplayName(Thesaurus thesaurus) {
        return thesaurus.getString(getTranslationKey(this), this.getDefaultFormat());
    }

    public static String getTranslationKey(EndPointAuthentication authentication) {
        return "webservices.authentication." + authentication.name().toLowerCase();
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getKey() {
        return name();
    }

    @Override
    public String getDefaultFormat() {
        return toString();
    }
}
