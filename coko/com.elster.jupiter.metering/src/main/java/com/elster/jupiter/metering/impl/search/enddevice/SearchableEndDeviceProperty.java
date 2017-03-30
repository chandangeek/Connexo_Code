/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.search.enddevice;

import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.util.conditions.Condition;

/**
 * Adds behavior to {@link SearchableProperty} that applies
 * to EndDevice related properties only and that will
 * assist in generating actual query {@link Condition}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-02 (15:01)
 */
public interface SearchableEndDeviceProperty extends SearchableProperty {

    /**
     * Creates an actual query {@link Condition}
     * from the specified specification.
     *
     * @param specification The condition specification
     * @return The Condition
     */
    Condition toCondition(Condition specification);

}