package com.energyict.protocolimplv2.messages.nls;

import com.energyict.mdc.upl.nls.TranslationKey;

import com.energyict.protocolimpl.properties.DescriptionTranslationKey;

/**
 * Provides an implementatin for the {@link TranslationKey} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-30 (09:43)
 */
public class TranslationKeyImpl implements TranslationKey {
    private final String key;
    private final String defaultFormat;

    public TranslationKeyImpl(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    public TranslationKey description() {
        return new DescriptionTranslationKey(this);
    }

}