package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.device.data.security.Privileges;
import java.util.Optional;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/communicationheatmap")
public class CommunicationHeatMapResource {

    private final Thesaurus thesaurus;
    private final DashboardService dashboardService;
    private final MeteringGroupsService meteringGroupService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public CommunicationHeatMapResource(Thesaurus thesaurus, DashboardService dashboardService, MeteringGroupsService meteringGroupService, ExceptionFactory exceptionFactory) {
        this.thesaurus = thesaurus;
        this.dashboardService = dashboardService;
        this.meteringGroupService = meteringGroupService;
        this.exceptionFactory = exceptionFactory;
    }

    /**
     * Generates data in heatmap format according to the requested breakdown
     *
     * @return HeatMap data
     * @throws Exception
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public CommunicationHeatMapInfo getConnectionHeatMap(@BeanParam JsonQueryFilter jsonQueryFilter) throws Exception {
        if (jsonQueryFilter.hasProperty(Constants.DEVICE_GROUP)) {
            Optional<EndDeviceGroup> deviceGroupOptional = meteringGroupService.findEndDeviceGroup(jsonQueryFilter.getLong(Constants.DEVICE_GROUP));
            return deviceGroupOptional
                    .map(g -> new CommunicationHeatMapInfo(dashboardService.getCommunicationTasksHeatMap(g), thesaurus))
                    .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_END_DEVICE_GROUP));
        }
        return new CommunicationHeatMapInfo(dashboardService.getCommunicationTasksHeatMap(), thesaurus);
    }

}