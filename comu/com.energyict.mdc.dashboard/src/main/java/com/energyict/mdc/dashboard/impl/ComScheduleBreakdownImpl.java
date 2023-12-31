/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.common.scheduling.ComSchedule;
import com.energyict.mdc.dashboard.ComScheduleBreakdown;

/**
 * Provides an implementation for the {@link ComScheduleBreakdown} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-25 (11:00)
 */
class ComScheduleBreakdownImpl extends TaskStatusBreakdownCountersImpl<ComSchedule> implements ComScheduleBreakdown {
    ComScheduleBreakdownImpl() {
        super();
    }
}