package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.energyict.mdc.upl.nls.TranslationKey;

/**
 * Adapter between UPL TranslationKey and Connexo TranslationKey.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-28 (09:43)
 */
class UPLTranslationKeyAdapter implements TranslationKey {
    private final com.elster.jupiter.nls.TranslationKey actual;

    public static TranslationKey adaptTo(com.elster.jupiter.nls.TranslationKey actual) {
        if (actual instanceof ConnexoTranslationKeyAdapter) {
            return ((ConnexoTranslationKeyAdapter) actual).getUplTranslationKey();
        } else {
            return new UPLTranslationKeyAdapter(actual);
        }
    }

    private UPLTranslationKeyAdapter(com.elster.jupiter.nls.TranslationKey actual) {
        this.actual = actual;
    }

    public com.elster.jupiter.nls.TranslationKey getConnexoTranslationKey() {
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
        if (obj instanceof UPLTranslationKeyAdapter) {
            return actual.equals(((UPLTranslationKeyAdapter) obj).actual);
        } else {
            return actual.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        return actual != null ? actual.hashCode() : 0;
    }
}