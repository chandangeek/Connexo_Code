package com.energyict.mdc.dashboard;

import aQute.bnd.annotation.ProviderType;

/**
 * Counts occurrences of things.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-29 (17:30)
 */
@ProviderType
public interface Counter<T> {

    /**
     * The number of times the target was observed by the
     * process that is counting.
     *
     * @return The count
     */
    public long getCount();

    /**
     * Returns the target of the counter, i.e. the object whose occurrence was counted.
     *
     * @return The target of the counter
     */
    public T getCountTarget();

}