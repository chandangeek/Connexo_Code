package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;

import com.energyict.mdc.dashboard.ComPortPoolBreakdown;
import com.energyict.mdc.dashboard.ComSessionSuccessIndicatorOverview;
import com.energyict.mdc.dashboard.TaskStatusOverview;
import com.energyict.mdc.dashboard.ConnectionTypeBreakdown;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.model.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by bvn on 7/29/14.
 */
@Path("/connectionoverview")
public class ConnectionOverviewResource {

    private final Thesaurus thesaurus;
    private final DashboardService dashboardService;
    private final OverviewFactory overviewFactory;
    private final BreakdownFactory breakdownFactory;
    private final DeviceDataService deviceDataService;

    @Inject
    public ConnectionOverviewResource(Thesaurus thesaurus, DashboardService dashboardService, OverviewFactory overviewFactory, BreakdownFactory breakdownFactory, DeviceDataService deviceDataService) {
        this.thesaurus = thesaurus;
        this.dashboardService = dashboardService;
        this.overviewFactory = overviewFactory;
        this.breakdownFactory = breakdownFactory;
        this.deviceDataService = deviceDataService;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_COMSERVER)
    public ConnectionOverviewInfo getConnectionOverview() throws Exception {
        TaskStatusOverview taskStatusOverview = dashboardService.getConnectionTaskStatusOverview();
        ComSessionSuccessIndicatorOverview comSessionSuccessIndicatorOverview = dashboardService.getComSessionSuccessIndicatorOverview();
        ComPortPoolBreakdown comPortPoolBreakdown = dashboardService.getComPortPoolBreakdown();
        ConnectionTypeBreakdown connectionTypeBreakdown = dashboardService.getConnectionTypeBreakdown();
        DeviceTypeBreakdown deviceTypeBreakdown = dashboardService.getConnectionTasksDeviceTypeBreakdown();
        ConnectionSummaryData connectionSummaryData = new ConnectionSummaryData(taskStatusOverview, deviceDataService.countWaitingConnectionTasksLastComSessionsWithAtLeastOneFailedTask());

            return new ConnectionOverviewInfo(connectionSummaryData, taskStatusOverview, comSessionSuccessIndicatorOverview,
                    comPortPoolBreakdown, connectionTypeBreakdown, deviceTypeBreakdown, breakdownFactory, overviewFactory,
                    thesaurus);
    }

}
