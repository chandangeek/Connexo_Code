package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.dashboard.DashboardService;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/connectionheatmap")
public class ConnectionHeatMapResource {

    private final Thesaurus thesaurus;
    private final DashboardService dashboardService;

    @Inject
    public ConnectionHeatMapResource(Thesaurus thesaurus, DashboardService dashboardService) {
        this.thesaurus = thesaurus;
        this.dashboardService = dashboardService;
    }

    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getConnectionHeatMap(@BeanParam JsonQueryFilter jsonQueryFilter) throws Exception {
        if (!jsonQueryFilter.getFilterProperties().containsKey("breakdown")) {
            Response.status(Response.Status.BAD_REQUEST).build();
        }
        BreakdownOption breakdown = jsonQueryFilter.getProperty("breakdown", new BreakdownOptionAdapter());

        return Response.ok(new ConnectionHeatMapInfo(
                breakdown==BreakdownOption.comPortPool?dashboardService.getComPortPoolHeatMap():(breakdown==BreakdownOption.connectionType?dashboardService.getConnectionTypeHeatMap():dashboardService.getDeviceTypeHeatMap()),
                breakdown,
                thesaurus)).build();
    }

}
