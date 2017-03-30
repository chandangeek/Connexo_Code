/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import java.util.List;

/**
 * This JSON representation holds the entire communication overview
 * @link http://confluence.eict.vpdc/display/JUP/Communications
 */
public class CommunicationOverviewInfo {

    public SummaryInfo communicationSummary;
    public List<TaskSummaryInfo> overviews;
    public List<BreakdownSummaryInfo> breakdowns;
    public DeviceGroupFilterInfo deviceGroup;
    public KpiInfo kpi;

    public CommunicationOverviewInfo() {
    }

}

