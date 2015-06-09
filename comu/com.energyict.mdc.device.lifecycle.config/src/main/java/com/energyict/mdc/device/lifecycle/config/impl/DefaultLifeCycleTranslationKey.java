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

    DEFAULT_DEVICE_LIFE_CYCLE_NAME("dlc.standard.device.life.cycle", "Standard device life cycle"),
    TRANSITION_START_COMMISSIONING("#commissioning", "Start commissioning"),
    TRANSITION_ACTIVATE("#activated", "Activate"),
    TRANSITION_INSTALL_ACTIVE("#installed#activated", "Install active"),
    TRANSITION_INSTALL("#installed", "Install inactive"),
    TRANSITION_DEACTIVATE("#deactivated", "Deactivate"),
    TRANSITION_DEACTIVATE_DECOMMISSION("#deactivated#decomissioned", "Deactivate and decommission"),
    TRANSITION_DECOMMISSION("#decommissioned", "Decommission"),
    TRANSITION_REMOVE("#deleted", "Remove"),
    TRANSITION_REMOVE_FROM_STOCK("#fromStock#deleted","Remove from stock");

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