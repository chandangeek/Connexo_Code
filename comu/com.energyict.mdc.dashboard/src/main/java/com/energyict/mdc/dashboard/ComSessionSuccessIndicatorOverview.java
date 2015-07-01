package com.energyict.mdc.dashboard;

import com.energyict.mdc.device.data.tasks.history.ComSession;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the overview of the execution of {@link ComSession}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-29 (17:41)
 */
@ProviderType
public interface ComSessionSuccessIndicatorOverview extends DashboardCounters<ComSession.SuccessIndicator> {

    public long getAtLeastOneTaskFailedCount();

}