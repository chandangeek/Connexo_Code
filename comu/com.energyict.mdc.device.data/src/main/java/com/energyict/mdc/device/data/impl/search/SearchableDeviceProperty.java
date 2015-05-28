package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.util.conditions.Condition;

/**
 * Adds behavior to {@link SearchableProperty} that applies
 * to Device related properties only and that will
 * assist in generating actual query {@link Condition}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-28 (13:06)
 */
public interface SearchableDeviceProperty extends SearchableProperty {

    /**
     * Creates an actual query {@link Condition}
     * from the specified specification.
     *
     * @param specification The condition specification
     * @return The Condition
     */
    public Condition toCondition(Condition specification);

}