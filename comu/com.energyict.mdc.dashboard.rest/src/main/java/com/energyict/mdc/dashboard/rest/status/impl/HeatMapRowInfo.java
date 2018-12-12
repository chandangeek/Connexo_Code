/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import java.util.List;

/**
 * Created by bvn on 9/18/14.
 */
class HeatMapRowInfo {
    public String displayValue;
    public FilterOption alias;
    public Long id;
    public List<TaskCounterInfo> data;
}
