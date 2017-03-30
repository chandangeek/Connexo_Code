/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import java.util.List;

/**
 * Created by bvn on 8/19/14.
 */
public class CommunicationHeatMapInfo {

    private static final CompletionCodeTaskCounterInfoComparator completionCodeTaskCounterInfoComparator = new CompletionCodeTaskCounterInfoComparator();

    public HeatMapBreakdownOption breakdown;
    public FilterOption alias;
    public List<HeatMapRowInfo> heatMap;

    public CommunicationHeatMapInfo() {
    }

}