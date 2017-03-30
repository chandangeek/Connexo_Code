/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import java.util.List;

/**
 * Created by bvn on 9/18/14.
 */
class TaskSummaryInfo {
    public long total;
    public String displayName;
    public FilterOption alias;
    public List<TaskCounterInfo> counters;
}
