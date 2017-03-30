/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.ComCommandCompletionCodeOverview;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;

/**
 * Provides an implementation for the {@link ComCommandCompletionCodeOverview} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-25 (11:54)
 */
public class ComCommandCompletionCodeOverviewImpl extends DashboardCountersImpl<CompletionCode> implements ComCommandCompletionCodeOverview {

    public ComCommandCompletionCodeOverviewImpl() {
        super();
    }

}