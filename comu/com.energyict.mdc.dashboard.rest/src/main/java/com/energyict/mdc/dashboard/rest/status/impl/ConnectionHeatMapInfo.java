/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;

/**
 * Created by bvn on 8/19/14.
 */
public class ConnectionHeatMapInfo {

    @XmlJavaTypeAdapter(BreakdownOptionAdapter.class)
    public HeatMapBreakdownOption breakdown;
    public FilterOption alias;
    public List<HeatMapRowInfo> heatMap;

    public ConnectionHeatMapInfo() {
    }
}
