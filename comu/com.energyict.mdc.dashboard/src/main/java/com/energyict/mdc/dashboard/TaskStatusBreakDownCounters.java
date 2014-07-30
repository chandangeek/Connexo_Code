package com.energyict.mdc.dashboard;

/**
 * Bundles a number of {@link TaskStatusBreakdownCounter}s that will track occurrences of things.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (09:28)
 */
public interface TaskStatusBreakDownCounters<C> extends Iterable<TaskStatusBreakdownCounter<C>> {

    public long getTotalCount();

}