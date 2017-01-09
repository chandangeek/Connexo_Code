package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.upl.nls.TranslationKey;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 3/01/2017 - 10:36
 */
public class UPLTranslationKeyAdapter implements TranslationKey {
    private final com.elster.jupiter.nls.TranslationKey actual;

    public UPLTranslationKeyAdapter(com.elster.jupiter.nls.TranslationKey actual) {
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