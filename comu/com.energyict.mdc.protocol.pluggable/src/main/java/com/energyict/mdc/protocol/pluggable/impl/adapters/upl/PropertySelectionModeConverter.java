package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.energyict.mdc.upl.properties.PropertySelectionMode;

/**
 * Converts between {@link PropertySelectionMode upl} and
 * {@link com.elster.jupiter.properties.PropertySelectionMode Connexo}
 * PropertySelectionMode values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-09 (11:49)
 */
final class PropertySelectionModeConverter {

    static com.elster.jupiter.properties.PropertySelectionMode fromUpl(PropertySelectionMode selectionMode) {
        switch (selectionMode) {
            case NONE: {
                return com.elster.jupiter.properties.PropertySelectionMode.UNSPECIFIED;
            }
            case LIST: {
                return com.elster.jupiter.properties.PropertySelectionMode.LIST;
            }
            case COMBOBOX: {
                return com.elster.jupiter.properties.PropertySelectionMode.COMBOBOX;
            }
            case SEARCH_AND_SELECT: {
                return com.elster.jupiter.properties.PropertySelectionMode.SEARCH_AND_SELECT;
            }
            default: {
                throw new IllegalArgumentException("Unexpected upl property selection mode: " + selectionMode.name());
            }
        }
    }

    // Hide utility class constructor
    private PropertySelectionModeConverter() {}
}