package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.energyict.mdc.engine.model.security.Privileges;
import com.google.common.base.Optional;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by bvn on 7/29/14.
 */
@Path("/connectionoverview")
public class ConnectionOverviewResource {

    private final ConnectionOverviewInfoFactory connectionOverviewInfoFactory;
    private final MeteringGroupsService meteringGroupService;

    @Inject
    public ConnectionOverviewResource(ConnectionOverviewInfoFactory connectionOverviewInfoFactory, MeteringGroupsService meteringGroupService) {
        this.connectionOverviewInfoFactory = connectionOverviewInfoFactory;
        this.meteringGroupService = meteringGroupService;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_COMMUNICATION_INFRASTRUCTURE)
    public ConnectionOverviewInfo getConnectionOverview() throws Exception {
        return connectionOverviewInfoFactory.asInfo();
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_COMMUNICATION_INFRASTRUCTURE)
    @Path("/{mrid}")
    public ConnectionOverviewInfo getConnectionOverview(@PathParam("mrid") String mrid) throws Exception {

        Optional<EndDeviceGroup> endDeviceGroup = meteringGroupService.findEndDeviceGroup(mrid);

        return connectionOverviewInfoFactory.asInfo(endDeviceGroup.get());
    }

}