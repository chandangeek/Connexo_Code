package com.energyict.mdc.dashboard;

import com.energyict.mdc.device.data.tasks.history.CompletionCode;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the overview of the execution of {@link com.energyict.mdc.tasks.ComTask}s
 * in terms of the {@link CompletionCode}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-25 (11:53)
 */
@ProviderType
public interface ComCommandCompletionCodeOverview extends DashboardCounters<CompletionCode> {
}