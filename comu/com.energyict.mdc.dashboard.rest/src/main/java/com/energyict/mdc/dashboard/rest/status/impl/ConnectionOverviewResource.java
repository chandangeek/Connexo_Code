/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by bvn on 7/29/14.
 */
@Path("/connectionoverview")
public class ConnectionOverviewResource {
    private static final Logger LOGGER = Logger.getLogger(ConnectionOverviewResource.class.getName());// just for time measurement

    private final ConnectionOverviewInfoFactory connectionOverviewInfoFactory;
    private final MeteringGroupsService meteringGroupService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ConnectionOverviewResource(ConnectionOverviewInfoFactory connectionOverviewInfoFactory, MeteringGroupsService meteringGroupService, ExceptionFactory exceptionFactory) {
        this.connectionOverviewInfoFactory = connectionOverviewInfoFactory;
        this.meteringGroupService = meteringGroupService;
        this.exceptionFactory = exceptionFactory;
    }

    @GET @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
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

    @GET @Transactional
    @Path("/widget")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public ConnectionOverviewInfo getConnectionWidget(@BeanParam JsonQueryFilter filter) throws Exception {
        StopWatch watch = new StopWatch(true);// just for time measurement
        ConnectionOverviewInfo connectionOverviewInfo;// just for time measurement
        watch.start();// just for time measurement
        if (filter.hasProperty("deviceGroup")) {
           connectionOverviewInfo = meteringGroupService // just for time measurement
            //return meteringGroupService
                    .findEndDeviceGroup(filter.getLong("deviceGroup"))
                    .map(connectionOverviewInfoFactory::asWidgetInfo)
                    .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_END_DEVICE_GROUP));
        } else {
            connectionOverviewInfo = connectionOverviewInfoFactory.asWidgetInfo(); // just for time measurement
            //return connectionOverviewInfoFactory.asWidgetInfo();
        }
        watch.stop();// just for time measurement
        LOGGER.log(Level.WARNING, "CONM1163: method: getConnectionWidget; " + watch.toString()); // just for time measurement
        return connectionOverviewInfo;
    }
}