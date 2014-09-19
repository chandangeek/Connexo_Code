package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.dashboard.ComPortPoolBreakdown;
import com.energyict.mdc.dashboard.ComSessionSuccessIndicatorOverview;
import com.energyict.mdc.dashboard.ConnectionTypeBreakdown;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.dashboard.TaskStatusOverview;
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

    private final DashboardService dashboardService;
    private final ConnectionOverviewInfoFactory connectionOverviewInfoFactory;
    private final DeviceDataService deviceDataService;

    @Inject
    public ConnectionOverviewResource(DashboardService dashboardService, ConnectionOverviewInfoFactory connectionOverviewInfoFactory, DeviceDataService deviceDataService) {
        this.dashboardService = dashboardService;
        this.connectionOverviewInfoFactory = connectionOverviewInfoFactory;
        this.deviceDataService = deviceDataService;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_COMMUNICATION_INFRASTRUCTURE)
    public ConnectionOverviewInfo getConnectionOverview() throws Exception {
        TaskStatusOverview taskStatusOverview = dashboardService.getConnectionTaskStatusOverview();
        ComSessionSuccessIndicatorOverview comSessionSuccessIndicatorOverview = dashboardService.getComSessionSuccessIndicatorOverview();
        ComPortPoolBreakdown comPortPoolBreakdown = dashboardService.getComPortPoolBreakdown();
        ConnectionTypeBreakdown connectionTypeBreakdown = dashboardService.getConnectionTypeBreakdown();
        DeviceTypeBreakdown deviceTypeBreakdown = dashboardService.getConnectionTasksDeviceTypeBreakdown();
        SummaryData summaryData = new SummaryData(taskStatusOverview, deviceDataService.countWaitingConnectionTasksLastComSessionsWithAtLeastOneFailedTask());

            return connectionOverviewInfoFactory.from(summaryData, taskStatusOverview, comSessionSuccessIndicatorOverview,
                    comPortPoolBreakdown, connectionTypeBreakdown, deviceTypeBreakdown);
    }

}
