package com.energyict.mdc.upl.properties;

/**
 * Models the different modes of selecting a value for a {@link PropertySpec} in a user interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-21 (09:22)
 */
public enum PropertySelectionMode {
    /**
     * Represents <code>null</code> meaning that no selection
     * mechanism is to be used.
     */
    NONE,

    /**
     * Represents the PropertySelectionMode that will use a
     * combobox mechanism to allow users to select a value for a property.
     * A set of possible values should be available
     */
    COMBOBOX,

    /**
     * Represents the PropertySelectionMode that will use a
     * "search and select" mechanism to allow users to
     * search for an appropriate value first and then finally select a value.
     */
    SEARCH_AND_SELECT,

    /**
     * Represents the PropertySelectionMode that will use a
     * list mechanism to allow users to select a value for a property.
     * A set of possible values should be available.
     */
    LIST;

    public int getCode () {
        return ordinal();
    }

}