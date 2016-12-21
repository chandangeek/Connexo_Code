package com.energyict.mdc.device.topology.rest.impl;

import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.rest.GraphFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Copyrights EnergyICT
 * Date: 21/12/2016
 * Time: 15:21
 */
@Path("/topology")
public class TopologyGraphResource {

    private final DeviceService deviceService;
    private final TopologyService topologyService;
    private final Provider<GraphFactory> graphFactoryProvider;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public TopologyGraphResource(DeviceService deviceService,
                                 TopologyService topologyService,
                                 Provider<GraphFactory> graphFactoryProvider,
                                 ExceptionFactory exceptionFactory) {
        this.deviceService = deviceService;
        this.topologyService = topologyService;
        this.graphFactoryProvider = graphFactoryProvider;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
   // @RolesAllowed({Privileges.Constants.VIEW_DEVICE_LIFE_CYCLE})
    public Response getTopologyGraphById(@PathParam("id") Long id, @BeanParam JsonQueryParameters queryParams) {
        Device device = deviceService.findDeviceById(id).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICE_NOT_FOUND, id));
        return Response.ok(graphFactoryProvider.get().from(device)).build();
    }

}
