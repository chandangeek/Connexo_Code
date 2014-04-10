package com.energyict.mdc.pluggable.rest.impl.properties;

/**
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 11:43
 */
public enum PropertySelectionMode {
    /**
     * Represents <code>null</code> meaning that no selection
     * mechanism is to be used.
     */
    UNSPECIFIED(0),

    /**
     * Represents the PropertySelectionMode that will
     * use a combobox mechanism to allow users to select a value
     * for an attribute.
     * A set of possible values should be available
     */
    COMBOBOX(1),

    /**
     * Represents the PropertySelectionMode that will
     * use a "search and select" mechanism to allow users to
     * search for an appropriate value first with all of the supported
     * match conditions for the attribute type and then
     * finally select a value.
     */
    SEARCH_AND_SELECT(2),

    /**
     * Represents the PropertySelectionMode that will
     * use a list mechanism to allow users to select a value
     * for an attribute.
     * A set of possible values should be available.
     */
    LIST(3);

    private int code = 0;

    PropertySelectionMode(int code) {
        this.code = code;
    }

    public int getCode () {
        return code;
    }
}
