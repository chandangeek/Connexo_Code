package com.energyict.protocolimpl.properties;

import com.energyict.mdc.upl.nls.TranslationKey;

/**
 * Provides an implementation for the {@link TranslationKey} interface
 * that is targetted to the description of a property spec.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-30 (09:50)
 */
public class DescriptionTranslationKey implements TranslationKey {
    private final TranslationKey actualKey;

    public DescriptionTranslationKey(TranslationKey actualKey) {
        this.actualKey = actualKey;
    }

    @Override
    public String getKey() {
        return this.actualKey.getKey() + ".description";
    }

    @Override
    public String getDefaultFormat() {
        return "Description for " + this.actualKey.getKey();
    }

}