package com.energyict.mdc.device.topology.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.rest.GraphLayerService;
import com.energyict.mdc.device.topology.rest.info.GraphInfo;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URLDecoder;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 * Date: 21/12/2016
 * Time: 15:21
 */
@Path("/topology")
public class TopologyGraphResource {

    private final Thesaurus thesaurus;
    private final DeviceService deviceService;
    private final TopologyService topologyService;
    private final GraphLayerService graphLayerService;
    private final ExceptionFactory exceptionFactory;
    private final Clock clock;
    private final DeviceGraphFactory deviceGraphFactory;

    @Inject
    public TopologyGraphResource(DeviceService deviceService,
                                 TopologyService topologyService,
                                 GraphLayerService graphLayerService,
                                 ExceptionFactory exceptionFactory,
                                 Thesaurus thesaurus,
                                 Clock clock) {
        this.deviceService = deviceService;
        this.topologyService = topologyService;
        this.graphLayerService = graphLayerService;
        this.exceptionFactory = exceptionFactory;
        this.thesaurus = thesaurus;
        this.clock = clock;
        deviceGraphFactory = new DeviceGraphFactory(this.topologyService, this.graphLayerService, clock);
    }

    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getTopologyGraphByName(@PathParam("name") String name, @BeanParam JsonQueryFilter layerFilter, @BeanParam JsonQueryParameters queryParams) {
        if (layerFilter.hasFilters()) {
            List<String> encodedLayerNames = layerFilter.getStringList("layers");
            List<String>  decodedLayerNames = new ArrayList<>(encodedLayerNames.size());
            for (String encodedLayerName :encodedLayerNames){
                 decodedLayerNames.add(URLDecoder.decode(encodedLayerName));
            }
            activateGraphLayers(decodedLayerNames);
        }
        Device device = deviceService.findDeviceByName(name).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICE_NOT_FOUND, name));
        GraphInfo graphInfo =  deviceGraphFactory.from(device);
        return Response.ok(graphInfo).build();
    }

    @GET
    @Path("/graphlayers")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getGraphLayers() {
        List<String> layersNames = graphLayerService.getGraphLayers().stream().map(layer -> layer.getDisplayName(thesaurus)).collect(Collectors.toList());
        return Response.ok(layersNames.toArray()).build();
    }

    private void activateGraphLayers(final List<String> names ) {
        graphLayerService.getGraphLayers().stream().forEach((layer)-> {
            if (names.contains(layer.getDisplayName(thesaurus))){
                layer.activate();
            }else{
                layer.deActivate();
            }
        });
    }

}
