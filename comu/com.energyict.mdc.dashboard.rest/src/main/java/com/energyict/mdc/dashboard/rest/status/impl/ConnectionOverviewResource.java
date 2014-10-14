package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.common.rest.LongAdapter;
import com.energyict.mdc.engine.model.security.Privileges;
import com.google.common.base.Optional;
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
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_COMMUNICATION_INFRASTRUCTURE)
    public ConnectionOverviewInfo getConnectionOverview(@BeanParam JsonQueryFilter filter) throws Exception {

        if (filter.getProperty("deviceGroup")!=null) {
            Optional<QueryEndDeviceGroup> optional = meteringGroupService.findQueryEndDeviceGroup(filter.getProperty("deviceGroup", new LongAdapter()));
            if (!optional.isPresent()) {
                throw exceptionFactory.newException(MessageSeeds.NO_SUCH_END_DEVICE_GROUP);
            }
            return connectionOverviewInfoFactory.asInfo(optional.get());
        } else {
            return connectionOverviewInfoFactory.asInfo();
        }
    }

}