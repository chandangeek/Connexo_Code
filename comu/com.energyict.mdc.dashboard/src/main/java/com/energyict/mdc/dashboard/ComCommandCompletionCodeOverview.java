/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard;

import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.history.CompletionCode;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the overview of the execution of {@link ComTask}s
 * in terms of the {@link CompletionCode}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-25 (11:53)
 */
@ProviderType
public interface ComCommandCompletionCodeOverview extends DashboardCounters<CompletionCode> {
}