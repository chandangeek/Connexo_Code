package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

/**
 * Created by bvn on 6/10/16.
 */
public enum WebServiceType implements TranslationKey {
    INBOUND("Inbound"), OUTBOUND("Outbound");

    private String name;

    WebServiceType(String name) {
        this.name = name;
    }

    public String getDisplayName(Thesaurus thesaurus) {
        return thesaurus.getString(getTranslationKey(this), this.getDefaultFormat());
    }

    public static String getTranslationKey(WebServiceType ll) {
        return "webservices.direction." + ll.name().toLowerCase();
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
