package com.energyict.mdc.device.topology.rest.impl;

import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.GraphLayerService;
import com.energyict.mdc.device.topology.rest.info.DeviceNodeInfo;
import com.energyict.mdc.device.topology.rest.info.DeviceSummaryNodeInfo;
import com.energyict.mdc.device.topology.rest.info.GraphInfo;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URLDecoder;
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

    private final DeviceService deviceService;
    private final GraphLayerService graphLayerService;
    private final ExceptionFactory exceptionFactory;
    private final DeviceGraphFactory deviceGraphFactory;
    private final BundleContext bundleContext;

    @Inject
    public TopologyGraphResource(DeviceService deviceService,
                                 GraphLayerService graphLayerService,
                                 ExceptionFactory exceptionFactory,
                                 DeviceGraphFactory deviceGraphFactory, BundleContext bundleContext) {
        this.deviceService = deviceService;
        this.graphLayerService = graphLayerService;
        this.exceptionFactory = exceptionFactory;
        this.deviceGraphFactory = deviceGraphFactory;
        this.bundleContext = bundleContext;
    }

    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getTopologyGraphByName(@PathParam("name") String name, @BeanParam JsonQueryFilter layerFilter, @BeanParam JsonQueryParameters queryParams) {
        boolean forceRefresh = false; // do not use the cached graphInfo
        if (layerFilter.hasFilters()) {
            List<String> encodedLayerNames = layerFilter.getStringList("layers");
            List<String> decodedLayerNames = new ArrayList<>(encodedLayerNames.size());
            for (String encodedLayerName : encodedLayerNames) {
                decodedLayerNames.add(URLDecoder.decode(encodedLayerName));
            }
            activateGraphLayers(decodedLayerNames);
            if (layerFilter.hasProperty("refresh")) {
                forceRefresh = layerFilter.getBoolean("refresh");
            }
        }
        Device device = deviceService.findDeviceByName(name).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICE_NOT_FOUND, name));
        GraphInfo graphInfo = deviceGraphFactory.forceRefresh(forceRefresh).from(device);
        return Response.ok(graphInfo).build();
    }

    @GET
    @Path("/graphlayers")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getGraphLayers() {
        List<String> layersNames = graphLayerService.getGraphLayers().stream().map(GraphLayer::getName).collect(Collectors.toList());
        return Response.ok(layersNames.toArray()).build();
    }

    @GET
    @Path("/configuration")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getConfiguration() {
        GeoLocationConfigurationProperties geoLocationConfigurationProperties = new GeoLocationConfigurationProperties(bundleContext);
        return Response.ok(geoLocationConfigurationProperties.buildGeoLocationConfigurationInfo()).build();
    }

    @GET
    @Path("/summary/{name}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getSummaryInfo(@PathParam("name") String name) {
        Device device = deviceService.findDeviceByName(name).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICE_NOT_FOUND, name));
        DeviceNodeInfo deviceNodeInfo = deviceGraphFactory.getNode(device).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICE_NOT_FOUND_IN_ANY_GRAPH, name));
        DeviceSummaryNodeInfo deviceSummaryNodeInfo = DeviceSummaryNodeInfo.of(deviceNodeInfo).withDevice(device);
        graphLayerService.getAllSummaryLayers().stream().forEach(deviceSummaryNodeInfo::addLayer);
        return Response.ok(deviceSummaryNodeInfo).build();
    }

    private void activateGraphLayers(final List<String> names) {
        graphLayerService.getGraphLayers().stream().forEach((layer) -> layer.setActive(names.contains(layer.getName())));
    }
}
