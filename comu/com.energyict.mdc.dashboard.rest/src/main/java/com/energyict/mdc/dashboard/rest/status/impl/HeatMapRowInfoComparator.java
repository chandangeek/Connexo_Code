/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

/**
 * Created by bvn on 8/29/14.
 */
public class HeatMapRowInfoComparator implements java.util.Comparator<HeatMapRowInfo> {

    @Override
    public int compare(HeatMapRowInfo heatMapRowInfo1, HeatMapRowInfo heatMapRowInfo2) {
        return heatMapRowInfo1.displayValue.compareTo(heatMapRowInfo2.displayValue);
    }
}
