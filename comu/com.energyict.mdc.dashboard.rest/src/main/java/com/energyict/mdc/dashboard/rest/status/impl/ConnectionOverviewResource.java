package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.dashboard.ComPortPoolBreakdown;
import com.energyict.mdc.dashboard.ComSessionSuccessIndicatorOverview;
import com.energyict.mdc.dashboard.ConnectionStatusOverview;
import com.energyict.mdc.dashboard.ConnectionTypeBreakdown;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.dashboard.HeatMap;
import com.energyict.mdc.dashboard.TaskStatusBreakdownCounter;
import com.energyict.mdc.tasks.history.CompletionCode;
import java.util.HashMap;
import java.util.Map;
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
            BreakdownOption breakdown = jsonQueryFilter.getProperty("breakdown", new BreakdownOptionAdapter());
            ConnectionStatusOverview connectionStatusOverview = dashboardService.getConnectionStatusOverview();
            ComSessionSuccessIndicatorOverview comSessionSuccessIndicatorOverview = dashboardService.getComSessionSuccessIndicatorOverview();
            ComPortPoolBreakdown comPortPoolBreakdown = dashboardService.getComPortPoolBreakdown();
            ConnectionTypeBreakdown connectionTypeBreakdown = dashboardService.getConnectionTypeBreakdown();
            DeviceTypeBreakdown deviceTypeBreakdown = dashboardService.getDeviceTypeBreakdown();
            HeatMap<?> heatMap = null;
            switch (breakdown) {
                case comPortPool:
                    heatMap=dashboardService.getComPortPoolHeatMap();
                    break;
                case connectionType:
                    heatMap=dashboardService.getConnectionTypeHeatMap();
                    break;
                case deviceType:
                    heatMap=dashboardService.getDeviceTypeHeatMap();
                    break;
            }
            ConnectionSummaryData connectionSummaryData = new ConnectionSummaryData(connectionStatusOverview);
            info = new ConnectionOverviewInfo(connectionSummaryData, connectionStatusOverview, comSessionSuccessIndicatorOverview, comPortPoolBreakdown, connectionTypeBreakdown, deviceTypeBreakdown, heatMap,thesaurus);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }


    static class HeatMapData {
        Map<CompletionCode, TaskStatusBreakdownCounter<?>> map = new HashMap<>();
    }
}
