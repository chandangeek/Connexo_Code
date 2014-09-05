package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.engine.model.security.Privileges;

import javax.annotation.security.RolesAllowed;
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
    @RolesAllowed(Privileges.VIEW_COMSERVER)
    public ConnectionHeatMapInfo getConnectionHeatMap(@BeanParam JsonQueryFilter jsonQueryFilter) throws Exception {
        if (!jsonQueryFilter.getFilterProperties().containsKey("breakdown")) {
            Response.status(Response.Status.BAD_REQUEST).build();
        }
        HeatMapBreakdownOption breakdown = jsonQueryFilter.getProperty("breakdown", new BreakdownOptionAdapter());

        return new ConnectionHeatMapInfo(
                breakdown== HeatMapBreakdownOption.comPortPools?dashboardService.getConnectionsComPortPoolHeatMap():(breakdown== HeatMapBreakdownOption.connectionTypes?dashboardService.getConnectionTypeHeatMap():dashboardService.getConnectionsDeviceTypeHeatMap()),
                breakdown,
                thesaurus);
    }

}
