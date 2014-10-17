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

class DeviceGroupFilterInfo {
    public Object id;
    public String name;
    public String alias = FilterOption.deviceGroups.name(); // Should be in JSON answer

    public DeviceGroupFilterInfo() {
    }

    public DeviceGroupFilterInfo(long id, String name) {
        this.id = id;
        this.name = name;
    }
}


