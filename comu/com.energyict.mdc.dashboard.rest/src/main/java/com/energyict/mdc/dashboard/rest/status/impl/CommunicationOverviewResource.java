/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.time.StopWatch;
import com.energyict.mdc.device.data.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by bvn on 7/29/14.
 */
@Path("/communicationoverview")
public class CommunicationOverviewResource {
    private static final Logger LOGGER = Logger.getLogger(CommunicationOverviewResource.class.getName());// just for time measurement

    private final CommunicationOverviewInfoFactory communicationOverviewInfoFactory;
    private final MeteringGroupsService meteringGroupService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public CommunicationOverviewResource(CommunicationOverviewInfoFactory communicationOverviewInfoFactory, MeteringGroupsService meteringGroupService, ExceptionFactory exceptionFactory) {
        this.communicationOverviewInfoFactory = communicationOverviewInfoFactory;
        this.meteringGroupService = meteringGroupService;
        this.exceptionFactory = exceptionFactory;
    }

    @GET @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public CommunicationOverviewInfo getCommunicationOverview(@BeanParam JsonQueryFilter filter) throws Exception {
        StopWatch watch = new StopWatch(true);// just for time measurement
        CommunicationOverviewInfo communicationOverviewInfo;// just for time measurement
        if (filter.hasProperty("deviceGroup")) {
            EndDeviceGroup endDeviceGroup = meteringGroupService.findEndDeviceGroup(filter.getLong("deviceGroup"))
                    .orElseThrow(()->exceptionFactory.newException(MessageSeeds.NO_SUCH_END_DEVICE_GROUP));
            //return communicationOverviewInfoFactory.asInfo(endDeviceGroup);
            communicationOverviewInfo = communicationOverviewInfoFactory.asInfo(endDeviceGroup);// just for time measurement
        } else {
            //return communicationOverviewInfoFactory.asInfo();
            communicationOverviewInfo = communicationOverviewInfoFactory.asInfo();// just for time measurement
        }
        watch.stop();// just for time measurement
        LOGGER.log(Level.WARNING, "CONM1163: method: getCommunicationOverview; " + watch.toString()); // just for time measurement
        return communicationOverviewInfo;
    }

    @GET @Transactional
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
