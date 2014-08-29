package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dashboard.DashboardService;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/communicationheatmap")
public class CommunicationHeatMapResource {

    private final Thesaurus thesaurus;
    private final DashboardService dashboardService;

    @Inject
    public CommunicationHeatMapResource(Thesaurus thesaurus, DashboardService dashboardService) {
        this.thesaurus = thesaurus;
        this.dashboardService = dashboardService;
    }

    /**
     * Generates data in heatmap format according to the requested breakdown
     *
     * @return HeatMap data
     * @throws Exception
     */
    @GET
    @Produces("application/json")
    public CommunicationHeatMapInfo getConnectionHeatMap() throws Exception {
        return new CommunicationHeatMapInfo(dashboardService.getCommunicationTasksHeatMap(),thesaurus);
    }

}
