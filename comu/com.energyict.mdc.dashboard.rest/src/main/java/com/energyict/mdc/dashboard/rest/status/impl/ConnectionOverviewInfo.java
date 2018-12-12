/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import java.util.List;

/**
 * This JSON representation holds the entire connection overview
 * @link http://confluence.eict.vpdc/display/JUP/Connections
 * Created by bvn on 7/29/14.
 */
public class ConnectionOverviewInfo {

    public SummaryInfo connectionSummary;

    public List<TaskSummaryInfo> overviews;
    public List<BreakdownSummaryInfo> breakdowns;
    public KpiInfo kpi;
    public DeviceGroupFilterInfo deviceGroup;

    public ConnectionOverviewInfo() {
    }

}



