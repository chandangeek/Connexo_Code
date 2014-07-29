package com.energyict.mdc.dashboard;

/**
 * Counts occurrences of things.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-29 (17:30)
 */
public interface Counter<T> {

    public long getCount();

    public T getCountTarget();

}