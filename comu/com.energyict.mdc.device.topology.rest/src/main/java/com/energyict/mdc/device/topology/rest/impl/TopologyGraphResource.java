package com.energyict.mdc.device.topology.rest.impl;

import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.rest.GraphLayerService;
import com.energyict.mdc.device.topology.rest.info.GraphInfo;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.time.Instant;

/**
 * Copyrights EnergyICT
 * Date: 21/12/2016
 * Time: 15:21
 */
@Path("/topology")
public class TopologyGraphResource {

    private final DeviceService deviceService;
    private final TopologyService topologyService;
    private final GraphLayerService graphLayerService;
    private final ExceptionFactory exceptionFactory;
    private final Clock clock;

    @Inject
    public TopologyGraphResource(DeviceService deviceService,
                                 TopologyService topologyService,
                                 GraphLayerService graphLayerService,
                                 ExceptionFactory exceptionFactory,
                                 Clock clock) {
        this.deviceService = deviceService;
        this.topologyService = topologyService;
        this.graphLayerService = graphLayerService;
        this.exceptionFactory = exceptionFactory;
        this.clock = clock;
    }

    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
   // @RolesAllowed({Privileges.Constants.VIEW_DEVICE_LIFE_CYCLE})
    // @DeviceStatesRestricted({DefaultState.IN_STOCK, DefaultState.DECOMMISSIONED})
    public Response getTopologyGraphByName(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParams) {
        Device device = deviceService.findDeviceByName(name).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICE_NOT_FOUND, name));
        GraphInfo graphInfo =  new DeviceGraphFactory(this.topologyService, this.graphLayerService, this.clock).from(device);
        return Response.ok(graphInfo, MediaType.APPLICATION_JSON_TYPE).build();
    }

}
