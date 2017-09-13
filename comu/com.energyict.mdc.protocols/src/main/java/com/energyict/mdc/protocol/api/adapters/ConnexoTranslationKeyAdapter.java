/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.adapters;

import com.energyict.mdc.upl.nls.TranslationKey;

/**
 * Adapter between UPL TranslationKey and Connexo TranslationKey.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-28 (09:43)
 */
public class ConnexoTranslationKeyAdapter implements com.elster.jupiter.nls.TranslationKey {

    private final TranslationKey actual;

    public static com.elster.jupiter.nls.TranslationKey adaptTo(TranslationKey actual) {
        return new ConnexoTranslationKeyAdapter(actual);
    }

    private ConnexoTranslationKeyAdapter(TranslationKey actual) {
        this.actual = actual;
    }

    public TranslationKey getUplTranslationKey() {
        return actual;
    }

    @Override
    public String getKey() {
        return this.actual.getKey();
    }

    @Override
    public String getDefaultFormat() {
        return this.actual.getDefaultFormat();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConnexoTranslationKeyAdapter) {
            return actual.equals(((ConnexoTranslationKeyAdapter) obj).actual);
        } else {
            return actual.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        return actual != null ? actual.hashCode() : 0;
    }
}