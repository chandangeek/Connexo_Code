package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.data.security.Privileges;
import java.util.Optional;
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
@Path("/communicationoverview")
public class CommunicationOverviewResource {

    private final CommunicationOverviewInfoFactory communicationOverviewInfoFactory;
    private final MeteringGroupsService meteringGroupService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public CommunicationOverviewResource(CommunicationOverviewInfoFactory communicationOverviewInfoFactory, MeteringGroupsService meteringGroupService, ExceptionFactory exceptionFactory) {
        this.communicationOverviewInfoFactory = communicationOverviewInfoFactory;
        this.meteringGroupService = meteringGroupService;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public CommunicationOverviewInfo getCommunicationOverview(@BeanParam JsonQueryFilter filter) throws Exception {
        if (filter.hasProperty("deviceGroup")) {
            EndDeviceGroup endDeviceGroup = meteringGroupService.findEndDeviceGroup(filter.getLong("deviceGroup"))
                    .orElseThrow(()->exceptionFactory.newException(MessageSeeds.NO_SUCH_END_DEVICE_GROUP));
            return communicationOverviewInfoFactory.asInfo(endDeviceGroup);
        } else {
            return communicationOverviewInfoFactory.asInfo();
        }
    }

    @GET
    @Path("/widget")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public CommunicationOverviewInfo getCommunicationWidget(@BeanParam JsonQueryFilter filter) throws Exception {
        if (filter.hasProperty("deviceGroup")) {
            Optional<EndDeviceGroup> optional = meteringGroupService.findEndDeviceGroup(filter.getLong("deviceGroup"));
            if (!optional.isPresent()) {
                throw exceptionFactory.newException(MessageSeeds.NO_SUCH_END_DEVICE_GROUP);
            }
            return communicationOverviewInfoFactory.asWidgetInfo(optional.get());
        } else {
            return communicationOverviewInfoFactory.asWidgetInfo();
        }
    }
}
