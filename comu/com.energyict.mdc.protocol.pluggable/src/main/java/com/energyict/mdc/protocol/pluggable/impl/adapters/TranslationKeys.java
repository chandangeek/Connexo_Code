package com.energyict.mdc.protocol.pluggable.impl.adapters;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-27 (11:29)
 */
public enum TranslationKeys implements TranslationKey {

    LEGACY_PROTOCOL("AdapterDeviceProtocolDialect", "Default");

    private final String uniqueName;
    private final String defaultFormat;

    TranslationKeys(String uniqueName, String defaultFormat) {
        this.uniqueName = uniqueName;
        this.defaultFormat = defaultFormat;
    }

    public String getName() {
        return uniqueName;
    }


    @Override
    public String getKey() {
        return this.getName();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

}