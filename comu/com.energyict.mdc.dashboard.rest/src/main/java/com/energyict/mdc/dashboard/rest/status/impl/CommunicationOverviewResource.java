package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dashboard.ComCommandCompletionCodeOverview;
import com.energyict.mdc.dashboard.ComScheduleBreakdown;
import com.energyict.mdc.dashboard.ComTaskBreakdown;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.dashboard.TaskStatusOverview;
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
@Path("/communicationoverview")
public class CommunicationOverviewResource {

    private final Thesaurus thesaurus;
    private final DashboardService dashboardService;
    private final BreakdownFactory breakdownFactory;
    private final OverviewFactory overviewFactory;

    @Inject
    public CommunicationOverviewResource(Thesaurus thesaurus, DashboardService dashboardService, BreakdownFactory breakdownFactory, OverviewFactory overviewFactory) {
        this.thesaurus = thesaurus;
        this.dashboardService = dashboardService;
        this.breakdownFactory = breakdownFactory;
        this.overviewFactory = overviewFactory;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_COMSERVER)
    public CommunicationOverviewInfo getCommunicationOverview() throws Exception {
        TaskStatusOverview taskStatusOverview = dashboardService.getCommunicationTaskStatusOverview();
        SummaryData summaryData = new SummaryData(taskStatusOverview);
        ComCommandCompletionCodeOverview comSessionSuccessIndicatorOverview = dashboardService.getCommunicationTaskCompletionResultOverview();
        ComScheduleBreakdown comScheduleBreakdown = dashboardService.getCommunicationTasksComScheduleBreakdown();
        ComTaskBreakdown comTaskBreakdown = dashboardService.getCommunicationTasksBreakdown();
        DeviceTypeBreakdown deviceTypeBreakdown = dashboardService.getCommunicationTasksDeviceTypeBreakdown();

        return new CommunicationOverviewInfo(summaryData, taskStatusOverview, comSessionSuccessIndicatorOverview, comScheduleBreakdown, comTaskBreakdown, deviceTypeBreakdown,
                breakdownFactory, overviewFactory, thesaurus);
    }

}
