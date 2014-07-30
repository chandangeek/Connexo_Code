package com.energyict.mdc.dashboard;

/**
 * Bundles a number of {@link Counter}s that will track occurrences of things.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-29 (17:33)
 */
public interface DashboardCounters<T> extends Iterable<Counter<T>>  {

    public long getTotalCount();

}