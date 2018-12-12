/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

public enum PropertySelectionMode {
    /**
     * Represents <code>null</code> meaning that no selection
     * mechanism is to be used.
     */
    UNSPECIFIED,

    /**
     * Represents the PropertySelectionMode that will
     * use a combobox mechanism to allow users to select a value
     * for an attribute.
     * A set of possible values should be available
     */
    COMBOBOX,

    /**
     * Represents the PropertySelectionMode that will
     * use a "search and select" mechanism to allow users to
     * search for an appropriate value first with all of the supported
     * match conditions for the attribute type and then
     * finally select a value.
     */
    SEARCH_AND_SELECT,

    /**
     * Represents the PropertySelectionMode that will
     * use a list mechanism to allow users to select a value
     * for an attribute.
     * A set of possible values should be available.
     */
    LIST;

    public int getCode () {
        return ordinal();
    }

}