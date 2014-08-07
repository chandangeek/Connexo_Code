package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.ComSessionSuccessIndicatorOverview;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.tasks.history.ComSession;

/**
 * Provides an implementation for the {@link ComSessionSuccessIndicatorOverview} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (11:54)
 */
public class ComSessionSuccessIndicatorOverviewImpl extends DashboardCountersImpl<ComSession.SuccessIndicator> implements ComSessionSuccessIndicatorOverview {

    private final long atleastOneTaskFailedCount;

    public ComSessionSuccessIndicatorOverviewImpl(long atleastOneTaskFailedCount) {
        super();
        this.atleastOneTaskFailedCount = atleastOneTaskFailedCount;
    }

    public ComSessionSuccessIndicatorOverviewImpl(long atleastOneTaskFailedCount, Counter<ComSession.SuccessIndicator>... counters) {
        super(counters);
        this.atleastOneTaskFailedCount = atleastOneTaskFailedCount;
    }

    @Override
    public long getAtLeastOneTaskFailedCount() {
        return this.atleastOneTaskFailedCount;
    }

}