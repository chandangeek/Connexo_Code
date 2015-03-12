package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Represents the translation keys for the elements of the default life cycle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (11:42)
 */
public enum DefaultLifeCycleTranslationKey implements TranslationKey {

    DEFAULT_FINATE_STATE_MACHINE_NAME("dlc.default.finate.state.machine", "Default life cycle"),
    ORDERED_DEFAULT_STATE("dlc.default.ordered", "Ordered"),
    IN_STOCK_DEFAULT_STATE("dlc.default.inStock", "In Stock"),
    COMMISSIONED_DEFAULT_STATE("dlc.default.commissioned", "Commissioned"),
    ACTIVE_DEFAULT_STATE("dlc.default.active", "Active"),
    INACTIVE_DEFAULT_STATE("dlc.default.inactive", "Inactive"),
    DECOMMISSIONED_DEFAULT_STATE("dlc.default.decommissioned", "Decommissioned"),
    DELETED_DEFAULT_STATE("dlc.default.deleted", "Deleted");

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