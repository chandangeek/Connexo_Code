package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.dashboard.ComPortPoolBreakdown;
import com.energyict.mdc.dashboard.ComTaskCompletionOverview;
import com.energyict.mdc.dashboard.ConnectionStatusOverview;
import com.energyict.mdc.dashboard.ConnectionTypeBreakdown;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Created by bvn on 7/29/14.
 */
@Path("/connectionoverview")
public class ConnectionOverviewResource {

    private final Thesaurus thesaurus;
    private final DashboardService dashboardService;

    @Inject
    public ConnectionOverviewResource(Thesaurus thesaurus, DashboardService dashboardService) {
        this.thesaurus = thesaurus;
        this.dashboardService = dashboardService;
    }

    @GET
    @Consumes("application/json")
    public ConnectionOverviewInfo getConnectionOverview(@BeanParam JsonQueryFilter jsonQueryFilter) {
        ConnectionOverviewInfo info = null;
        try {
            FilterOption breakdown = jsonQueryFilter.getProperty("breakdown", new FilterOptionAdapter());
            ConnectionStatusOverview connectionStatusOverview = dashboardService.getConnectionStatusOverview();
            ComTaskCompletionOverview comTaskCompletionOverview = dashboardService.getComTaskCompletionOverview();
            ComPortPoolBreakdown comPortPoolBreakdown = dashboardService.getComPortPoolBreakdown();
            ConnectionTypeBreakdown connectionTypeBreakdown = dashboardService.getConnectionTypeBreakdown();
            DeviceTypeBreakdown deviceTypeBreakdown = dashboardService.getDeviceTypeBreakdown();
            ConnectionSummaryData connectionSummaryData = new ConnectionSummaryData(connectionStatusOverview);
            info = new ConnectionOverviewInfo(connectionSummaryData, connectionStatusOverview, comTaskCompletionOverview, comPortPoolBreakdown, connectionTypeBreakdown, deviceTypeBreakdown, breakdown,thesaurus);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }


}
