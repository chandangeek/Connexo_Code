package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Represents the translation keys for the elements of the default life cycle.
 * Note that the information about the {@link com.elster.jupiter.fsm.State}s
 * has been extracted into {@link com.energyict.mdc.device.lifecycle.config.DefaultState}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (11:42)
 */
public enum DefaultLifeCycleTranslationKey implements TranslationKey {

    DEFAULT_DEVICE_LIFE_CYCLE_NAME("dlc.standard.device.life.cycle", "Standard device life cycle");

    private final String key;
    private final String defaultFormat;

    DefaultLifeCycleTranslationKey(String key, String defaultFormat) {
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

}