package com.energyict.mdc.device.lifecycle.config;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.TranslationKey;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Models the {@link com.elster.jupiter.fsm.State}s
 * that are part of the default {@link DeviceLifeCycle}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-12 (10:37)
 */
public enum DefaultState implements TranslationKey {

    IN_STOCK("dlc.default.inStock", "In stock"),
    COMMISSIONING("dlc.default.commissioning", "Commissioning"),
    ACTIVE("dlc.default.active", "Active"),
    INACTIVE("dlc.default.inactive", "Inactive"),
    DECOMMISSIONED("dlc.default.decommissioned", "Decommissioned"),
    REMOVED("dlc.default.removed", "Removed");

    private final String key;
    private final String defaultFormat;

    DefaultState(String key, String defaultFormat) {
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

    /**
     * Determines the DefaultState for the specified {@link State}.
     * Will return <code>Optional.empty()</code> when the State
     * is not standard or the symbolic name does not match
     * one of the standards.
     *
     * @param state The State
     * @return The DefaultState
     */
    public static Optional<DefaultState> from(State state) {
        if (!state.isCustom()) {
            String symbolicName = state.getName();
            return Stream
                    .of(DefaultState.values())
                    .filter(d -> d.getKey().equals(symbolicName))
                    .findFirst();
        }
        else {
            return Optional.empty();
        }
    }

}