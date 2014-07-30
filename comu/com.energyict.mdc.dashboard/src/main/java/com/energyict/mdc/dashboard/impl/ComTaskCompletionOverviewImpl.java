package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.ComTaskCompletionOverview;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.tasks.history.CompletionCode;

/**
 * Provides an implementation for the {@link ComTaskCompletionOverview} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (11:54)
 */
public class ComTaskCompletionOverviewImpl extends DashboardCountersImpl<CompletionCode> implements ComTaskCompletionOverview {

    public ComTaskCompletionOverviewImpl() {
        super();
    }

    public ComTaskCompletionOverviewImpl(Counter<CompletionCode>... counters) {
        super(counters);
    }

}