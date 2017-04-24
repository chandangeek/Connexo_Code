package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.energyict.mdc.upl.nls.TranslationKey;

/**
 * Adapter between UPL TranslationKey and Connexo TranslationKey.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-28 (09:43)
 */
public class ConnexoTranslationKeyAdapter implements com.elster.jupiter.nls.TranslationKey {
    private final TranslationKey actual;

    ConnexoTranslationKeyAdapter(TranslationKey actual) {
        this.actual = actual;
    }

    public TranslationKey getActual() {
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
}