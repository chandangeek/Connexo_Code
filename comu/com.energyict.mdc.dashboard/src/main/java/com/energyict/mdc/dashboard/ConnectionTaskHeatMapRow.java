package com.energyict.mdc.dashboard;

/**
 * Models one row of a {@link ConnectionTaskHeatMap} that focusses
 * on reporting {@link ComSessionSuccessIndicatorOverview}
 * for a single target.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-04 (16:59)
 */
public interface ConnectionTaskHeatMapRow<T> extends Iterable<ComSessionSuccessIndicatorOverview> {

    public T getTarget();

}