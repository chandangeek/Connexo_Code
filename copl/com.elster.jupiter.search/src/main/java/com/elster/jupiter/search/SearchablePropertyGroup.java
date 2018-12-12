/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search;

/**
 * Groups {@link SearchableProperty SearchableProperties}
 * to indicate to the UI that these properties are all
 * linked and should somehow be rendered as a group.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-01 (13:52)
 */
public interface SearchablePropertyGroup {

    /**
     * Gets a unique identifier for this SearchablePropertyGroup.
     *
     * @return The unique identifier
     */
    public String getId();

    public String getDisplayName();

}