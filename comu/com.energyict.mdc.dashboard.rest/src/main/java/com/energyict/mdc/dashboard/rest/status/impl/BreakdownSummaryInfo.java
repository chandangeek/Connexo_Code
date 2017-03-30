/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import java.util.List;

/**
 * Created by bvn on 9/18/14.
 */
class BreakdownSummaryInfo {
    public String displayName;
    public FilterOption alias;
    public long total;
    public long totalSuccessCount;
    public long totalPendingCount;
    public long totalFailedCount;
    public List<TaskBreakdownInfo> counters;
}
