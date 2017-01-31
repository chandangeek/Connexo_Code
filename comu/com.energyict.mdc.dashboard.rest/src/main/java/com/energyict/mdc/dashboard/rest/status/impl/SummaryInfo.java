/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import java.util.ArrayList;
import java.util.List;


/**
 * JSON representation of summary
 * JP-4278
 * Created by bvn on 7/29/14.
 */
public class SummaryInfo {
    public long total;
    public FilterOption alias=FilterOption.currentStates; // this needs to be in JSON! do not remove
    public List<? super TaskSummaryCounterInfo> counters = new ArrayList<>();
    public Long target;
}

