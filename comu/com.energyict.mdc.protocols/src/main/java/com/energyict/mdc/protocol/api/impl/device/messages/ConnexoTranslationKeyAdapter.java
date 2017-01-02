package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.upl.nls.TranslationKey;

/**
 * Adapter between UPL TranslationKey and Connexo TranslationKey.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-28 (09:43)
 */
class ConnexoTranslationKeyAdapter implements com.elster.jupiter.nls.TranslationKey {
    private final TranslationKey actual;

    ConnexoTranslationKeyAdapter(TranslationKey actual) {
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