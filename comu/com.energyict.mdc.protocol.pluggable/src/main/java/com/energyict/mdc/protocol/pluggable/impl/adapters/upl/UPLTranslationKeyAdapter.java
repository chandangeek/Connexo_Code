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

    UPLTranslationKeyAdapter(com.elster.jupiter.nls.TranslationKey actual) {
        this.actual = actual;
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