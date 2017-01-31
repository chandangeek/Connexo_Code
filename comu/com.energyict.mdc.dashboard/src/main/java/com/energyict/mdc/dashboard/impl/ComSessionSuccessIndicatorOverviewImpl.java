/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.ComSessionSuccessIndicatorOverview;
import com.energyict.mdc.device.data.tasks.history.ComSession;

import java.util.Map;

/**
 * Provides an implementation for the {@link ComSessionSuccessIndicatorOverview} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (11:54)
 */
public class ComSessionSuccessIndicatorOverviewImpl extends DashboardCountersImpl<ComSession.SuccessIndicator> implements ComSessionSuccessIndicatorOverview {

    private final long atleastOneTaskFailedCount;

    public static ComSessionSuccessIndicatorOverviewImpl from(long atleastOneTaskFailedCount, Map<ComSession.SuccessIndicator, Long> successIndicatorCount) {
        ComSessionSuccessIndicatorOverviewImpl overview = new ComSessionSuccessIndicatorOverviewImpl(atleastOneTaskFailedCount);
        for (ComSession.SuccessIndicator successIndicator : ComSession.SuccessIndicator.values()) {
            overview.add(new CounterImpl<>(successIndicator, successIndicatorCount.get(successIndicator)));
        }
        return overview;
    }

    public ComSessionSuccessIndicatorOverviewImpl(long atleastOneTaskFailedCount) {
        super();
        this.atleastOneTaskFailedCount = atleastOneTaskFailedCount;
    }

    @Override
    public long getAtLeastOneTaskFailedCount() {
        return this.atleastOneTaskFailedCount;
    }

}