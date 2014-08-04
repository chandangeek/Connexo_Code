package com.energyict.mdc.dashboard;

import com.energyict.mdc.tasks.history.ComSession;

/**
 * Models the overview of the execution of {@link ComSession}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-29 (17:41)
 */
public interface ComSessionSuccessIndicatorOverview extends DashboardCounters<ComSession.SuccessIndicator> {

    public long getAtleastOneTaskFailedCount();

}