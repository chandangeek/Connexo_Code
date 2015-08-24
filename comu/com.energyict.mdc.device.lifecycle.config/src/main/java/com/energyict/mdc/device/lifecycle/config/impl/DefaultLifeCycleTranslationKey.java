package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.DefaultCustomStateTransitionEventType;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

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

    TRANSITION_FROM_IN_STOCK_TO_COMMISSIONING(DefaultState.IN_STOCK.getKey()+DefaultCustomStateTransitionEventType.COMMISSIONING.getSymbol(), "Start commissioning"),
    TRANSITION_FROM_INACTIVE_TO_ACTIVE(DefaultState.INACTIVE.getKey()+DefaultCustomStateTransitionEventType.ACTIVATED.getSymbol(), "Activate"),
    TRANSITION_FROM_IN_STOCK_TO_ACTIVE(DefaultState.IN_STOCK.getKey()+DefaultCustomStateTransitionEventType.ACTIVATED.getSymbol(), "Install active"),
    TRANSITION_FROM_IN_STOCK_TO_INACTIVE(DefaultState.IN_STOCK.getKey()+DefaultCustomStateTransitionEventType.DEACTIVATED.getSymbol(), "Install inactive"),
    TRANSITION_FROM_COMMISSIONING_TO_INACTIVE(DefaultState.COMMISSIONING.getKey()+DefaultCustomStateTransitionEventType.DEACTIVATED.getSymbol(), "Install inactive"),
    TRANSITION_FROM_COMMISSIONING_TO_ACTIVE(DefaultState.COMMISSIONING.getKey()+DefaultCustomStateTransitionEventType.ACTIVATED.getSymbol(), "Install active"),
    TRANSITION_FROM_ACTIVE_TO_INACTIVE(DefaultState.ACTIVE.getKey()+DefaultCustomStateTransitionEventType.DEACTIVATED.getSymbol(), "Deactivate"),
    TRANSITION_FROM_ACTIVE_TO_DECOMMISSIONED(DefaultState.ACTIVE.getKey()+DefaultCustomStateTransitionEventType.DECOMMISSIONED.getSymbol(), "Decommission"),
    TRANSITION_FROM_INACTIVE_TO_DECOMMISSIONED(DefaultState.INACTIVE.getKey()+DefaultCustomStateTransitionEventType.DECOMMISSIONED.getSymbol(), "Decommission"),
    TRANSITION_FROM_DECOMMISSIONED_TO_REMOVED(DefaultState.DECOMMISSIONED.getKey()+DefaultCustomStateTransitionEventType.REMOVED.getSymbol(), "Remove"),
    TRANSITION_FROM_IN_STOCK_TO_REMOVED(DefaultState.IN_STOCK.getKey()+DefaultCustomStateTransitionEventType.REMOVED.getSymbol(), "Remove");

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