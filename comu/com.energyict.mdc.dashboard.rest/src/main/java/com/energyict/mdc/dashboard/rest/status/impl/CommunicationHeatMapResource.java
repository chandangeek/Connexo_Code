package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.common.rest.LongAdapter;
import com.energyict.mdc.dashboard.DashboardService;
import com.google.common.base.Optional;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

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
    @Produces("application/json")
    public CommunicationHeatMapInfo getConnectionHeatMap(@BeanParam JsonQueryFilter jsonQueryFilter) throws Exception {
        if (jsonQueryFilter.getProperty(Constants.DEVICE_GROUP) != null) {
            Optional<QueryEndDeviceGroup> deviceGroupOptional = meteringGroupService.findQueryEndDeviceGroup(jsonQueryFilter.getProperty(Constants.DEVICE_GROUP, new LongAdapter()));
            if (!deviceGroupOptional.isPresent()) {
                throw exceptionFactory.newException(MessageSeeds.NO_SUCH_END_DEVICE_GROUP);
            }
            return new CommunicationHeatMapInfo(dashboardService.getCommunicationTasksHeatMap(deviceGroupOptional.get()), thesaurus);
        }
        return new CommunicationHeatMapInfo(dashboardService.getCommunicationTasksHeatMap(), thesaurus);
    }
}
