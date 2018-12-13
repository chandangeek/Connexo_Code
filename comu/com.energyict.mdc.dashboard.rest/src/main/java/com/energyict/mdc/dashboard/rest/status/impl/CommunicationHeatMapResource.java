/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.device.data.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Optional;

@Path("/communicationheatmap")
public class CommunicationHeatMapResource {

    private final DashboardService dashboardService;
    private final MeteringGroupsService meteringGroupService;
    private final ExceptionFactory exceptionFactory;
    private final CommunicationHeatMapInfoFactory communicationHeatMapInfoFactory;

    @Inject
    public CommunicationHeatMapResource(DashboardService dashboardService, MeteringGroupsService meteringGroupService, ExceptionFactory exceptionFactory, CommunicationHeatMapInfoFactory communicationHeatMapInfoFactory) {
        this.dashboardService = dashboardService;
        this.meteringGroupService = meteringGroupService;
        this.exceptionFactory = exceptionFactory;
        this.communicationHeatMapInfoFactory = communicationHeatMapInfoFactory;
    }

    /**
     * Generates data in heatmap format according to the requested breakdown
     *
     * @return HeatMap data
     * @throws Exception
     */
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public CommunicationHeatMapInfo getConnectionHeatMap(@BeanParam JsonQueryFilter jsonQueryFilter) throws Exception {
        if (jsonQueryFilter.hasProperty(Constants.DEVICE_GROUP)) {
            Optional<EndDeviceGroup> deviceGroupOptional = meteringGroupService.findEndDeviceGroup(jsonQueryFilter.getLong(Constants.DEVICE_GROUP));
            return deviceGroupOptional
                    .map(dashboardService::getCommunicationTasksHeatMap)
                    .map(communicationHeatMapInfoFactory::from)
                    .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_END_DEVICE_GROUP));
        }
        return communicationHeatMapInfoFactory.from(dashboardService.getCommunicationTasksHeatMap());
    }

}