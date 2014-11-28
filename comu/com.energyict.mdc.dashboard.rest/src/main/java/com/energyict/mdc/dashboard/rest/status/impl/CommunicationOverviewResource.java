package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.common.rest.LongAdapter;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.engine.model.security.Privileges;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Optional;

/**
 * Created by bvn on 7/29/14.
 */
@Path("/communicationoverview")
public class CommunicationOverviewResource {

    private final DashboardService dashboardService;
    private final CommunicationOverviewInfoFactory communicationOverviewInfoFactory;
    private final MeteringGroupsService meteringGroupService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public CommunicationOverviewResource(DashboardService dashboardService, CommunicationOverviewInfoFactory communicationOverviewInfoFactory, MeteringGroupsService meteringGroupService, ExceptionFactory exceptionFactory) {
        this.dashboardService = dashboardService;
        this.communicationOverviewInfoFactory = communicationOverviewInfoFactory;
        this.meteringGroupService = meteringGroupService;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.ADMINISTRATE_COMMUNICATION_INFRASTRUCTURE,Privileges.VIEW_COMMUNICATION_INFRASTRUCTURE})
    public CommunicationOverviewInfo getCommunicationOverview(@BeanParam JsonQueryFilter filter) throws Exception {
        if (filter.getProperty("deviceGroup")!=null) {
            Optional<QueryEndDeviceGroup> optional = meteringGroupService.findQueryEndDeviceGroup(filter.getProperty("deviceGroup", new LongAdapter()));
            if (!optional.isPresent()) {
                throw exceptionFactory.newException(MessageSeeds.NO_SUCH_END_DEVICE_GROUP);
            }
            return communicationOverviewInfoFactory.asInfo(optional.get());
        } else {
            return communicationOverviewInfoFactory.asInfo();
        }
    }

}
