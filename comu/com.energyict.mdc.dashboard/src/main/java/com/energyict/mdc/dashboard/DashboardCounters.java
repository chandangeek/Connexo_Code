package com.energyict.mdc.dashboard;

/**
 * Bundles a number of {@link Counter}s that will track occurrences of things.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-29 (17:33)
 */
public interface DashboardCounters<C> extends Iterable<Counter<C>>  {

    public long getTotalCount();

}