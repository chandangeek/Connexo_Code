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
     * @param jsonQueryFilter QueryFilter for breakdown attribute (mandatory)
     * @return HeatMap data
     * @throws Exception
     */
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public ConnectionHeatMapInfo getConnectionHeatMap(@BeanParam JsonQueryFilter jsonQueryFilter) throws Exception {
        if (!jsonQueryFilter.getFilterProperties().containsKey("breakdown")) {
            Response.status(Response.Status.BAD_REQUEST).build();
        }
        BreakdownOption breakdown = jsonQueryFilter.getProperty("breakdown", new BreakdownOptionAdapter());

        return new ConnectionHeatMapInfo(
                breakdown==BreakdownOption.comPortPool?dashboardService.getConnectionsComPortPoolHeatMap():(breakdown==BreakdownOption.connectionType?dashboardService.getConnectionTypeHeatMap():dashboardService.getConnectionsDeviceTypeHeatMap()),
                breakdown,
                thesaurus);
    }

}
