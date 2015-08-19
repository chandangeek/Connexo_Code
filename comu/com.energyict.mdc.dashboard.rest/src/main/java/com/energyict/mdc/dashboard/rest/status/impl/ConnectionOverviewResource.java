package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.data.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
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

    private final ConnectionOverviewInfoFactory connectionOverviewInfoFactory;
    private final MeteringGroupsService meteringGroupService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ConnectionOverviewResource(ConnectionOverviewInfoFactory connectionOverviewInfoFactory, MeteringGroupsService meteringGroupService, ExceptionFactory exceptionFactory) {
        this.connectionOverviewInfoFactory = connectionOverviewInfoFactory;
        this.meteringGroupService = meteringGroupService;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public ConnectionOverviewInfo getConnectionOverview(@BeanParam JsonQueryFilter filter) throws Exception {
        if (filter.hasProperty("deviceGroup")) {
            return meteringGroupService
                    .findEndDeviceGroup(filter.getLong("deviceGroup"))
                    .map(connectionOverviewInfoFactory::asInfo)
                    .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_END_DEVICE_GROUP));
        } else {
            return connectionOverviewInfoFactory.asInfo();
        }
    }

    @GET
    @Path("/widget")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public ConnectionOverviewInfo getConnectionWidget(@BeanParam JsonQueryFilter filter) throws Exception {
        if (filter.hasProperty("deviceGroup")) {
            return meteringGroupService
                    .findEndDeviceGroup(filter.getLong("deviceGroup"))
                    .map(connectionOverviewInfoFactory::asWidgetInfo)
                    .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_END_DEVICE_GROUP));
        } else {
            return connectionOverviewInfoFactory.asWidgetInfo();
        }
    }
}