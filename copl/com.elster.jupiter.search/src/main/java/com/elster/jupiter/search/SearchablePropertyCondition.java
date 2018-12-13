/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.conditions.Condition;

/**
 * Models a {@link Condition} expressed against a {@link SearchableProperty}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-28 (09:15)
 */
@ProviderType
public interface SearchablePropertyCondition {

    /**
     * Gets the {@link SearchableProperty} against which
     * this condition is expressed.
     *
     * @return The SearchableProperty
     */
    public SearchableProperty getProperty();

    /**
     * Gets the actual {@link Condition} that is checked
     * against the {@link SearchableProperty}.
     *
     * @return The Condition
     */
    public Condition getCondition();

}