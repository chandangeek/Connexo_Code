package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.common.rest.LongAdapter;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.engine.model.security.Privileges;
import com.google.common.base.Optional;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@Path("/connectionheatmap")
public class ConnectionHeatMapResource {

    private final Thesaurus thesaurus;
    private final DashboardService dashboardService;
    private final MeteringGroupsService meteringGroupService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ConnectionHeatMapResource(Thesaurus thesaurus, DashboardService dashboardService, MeteringGroupsService meteringGroupService, ExceptionFactory exceptionFactory) {
        this.thesaurus = thesaurus;
        this.dashboardService = dashboardService;
        this.meteringGroupService = meteringGroupService;
        this.exceptionFactory = exceptionFactory;
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
    @RolesAllowed(Privileges.VIEW_COMMUNICATION_INFRASTRUCTURE)
    public ConnectionHeatMapInfo getConnectionHeatMap(@BeanParam JsonQueryFilter jsonQueryFilter) throws Exception {
        if (!jsonQueryFilter.getFilterProperties().containsKey("breakdown")) {
            throw new WebApplicationException("Missing breakdown", Response.Status.BAD_REQUEST);
        }
        HeatMapBreakdownOption breakdown = jsonQueryFilter.getProperty("breakdown", new BreakdownOptionAdapter());

        if (jsonQueryFilter.getProperty(Constants.DEVICE_GROUP)!=null) {
            Optional<QueryEndDeviceGroup> deviceGroupOptional = meteringGroupService.findQueryEndDeviceGroup(jsonQueryFilter.getProperty(Constants.DEVICE_GROUP, new LongAdapter()));
            if (!deviceGroupOptional.isPresent()) {
                throw exceptionFactory.newException(MessageSeeds.NO_SUCH_END_DEVICE_GROUP);
            }
            switch (breakdown) {
                case connectionTypes:
                    return new ConnectionHeatMapInfo(dashboardService.getConnectionTypeHeatMap(deviceGroupOptional.get()),breakdown,thesaurus);
                case deviceTypes:
                    return new ConnectionHeatMapInfo(dashboardService.getConnectionsDeviceTypeHeatMap(deviceGroupOptional.get()),breakdown,thesaurus);
                case comPortPools:
                    return new ConnectionHeatMapInfo(dashboardService.getConnectionsComPortPoolHeatMap(deviceGroupOptional.get()),breakdown,thesaurus);
                default:
                    throw new WebApplicationException("Invalid breakdown: "+breakdown, Response.Status.BAD_REQUEST);
            }
        }

        switch (breakdown) {
            case connectionTypes:
                return new ConnectionHeatMapInfo(dashboardService.getConnectionTypeHeatMap(),breakdown,thesaurus);
            case deviceTypes:
                return new ConnectionHeatMapInfo(dashboardService.getConnectionsDeviceTypeHeatMap(),breakdown,thesaurus);
            case comPortPools:
                return new ConnectionHeatMapInfo(dashboardService.getConnectionsComPortPoolHeatMap(),breakdown,thesaurus);
            default:
                throw new WebApplicationException("Invalid breakdown: "+breakdown, Response.Status.BAD_REQUEST);

        }

    }

}
