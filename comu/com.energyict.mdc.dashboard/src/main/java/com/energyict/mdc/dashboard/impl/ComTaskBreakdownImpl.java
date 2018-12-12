/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.ComTaskBreakdown;
import com.energyict.mdc.tasks.ComTask;

/**
 * Provides an implementation for the {@link ComTaskBreakdown} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-25 (08:35)
 */
class ComTaskBreakdownImpl extends TaskStatusBreakdownCountersImpl<ComTask> implements ComTaskBreakdown {
    ComTaskBreakdownImpl() {
        super();
    }
}