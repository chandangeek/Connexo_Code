package com.energyict.mdc.device.topology.rest.impl;

import com.elster.jupiter.util.geo.Elevation;
import com.elster.jupiter.util.geo.Latitude;
import com.elster.jupiter.util.geo.Longitude;
import com.elster.jupiter.util.geo.SpatialCoordinates;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.G3CommunicationPathSegment;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.rest.GraphFactory;
import com.energyict.mdc.device.topology.rest.GraphLayerService;
import com.energyict.mdc.device.topology.rest.GraphLayerType;
import com.energyict.mdc.device.topology.rest.info.DeviceNodeInfo;
import com.energyict.mdc.device.topology.rest.info.GraphInfo;
import com.energyict.mdc.device.topology.rest.info.NodeInfo;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 20/12/2016
 * Time: 17:16
 */

public class DeviceGraphFactory implements GraphFactory {

    private final TopologyService topologyService;
    private final GraphLayerService graphLayerService;
    private final Clock clock;
    private Map<Long, GraphInfo<Device>> cachedGraphs = new HashMap<>();
    private boolean forceRefresh;

    public DeviceGraphFactory(TopologyService topologyService, GraphLayerService graphLayerService, Clock clock) {
        this.topologyService = topologyService;
        this.graphLayerService = graphLayerService;
        this.clock = clock;
    }

    public DeviceGraphFactory forceRefresh(boolean forceRefresh) {
        this.forceRefresh = forceRefresh;
        return this;
    }

    public GraphInfo from(Device device) {
        Device gateway = this.topologyService.getPhysicalGateway(device).orElse(device);
        Instant now = clock.instant();
        if (!forceRefresh) {
            GraphInfo existing = cachedGraphs.get(gateway.getId());
            if (existing != null) {
                if (existing.isValid(now)) {
                    existing.setProperty("nodeCount", existing.size());
                    existing.setProperty("buildTime", Duration.between(now, clock.instant()).toMillis());
                    setNodeCoordinates(existing);
                    return existing;
                }
            }
        }
        forceRefresh = false;    // enable cashing of graphinfo's by default
        final DeviceNodeInfo rootNode = newNode(gateway);
        GraphInfo<Device> graphInfo = new GraphInfo<>(this.graphLayerService, clock.instant());
        graphInfo.setRootNode(rootNode);
        topologyService.getUniqueG3CommunicationPathSegments(gateway).forEach(s ->
                graphInfo.addNode(newNode(s))
        );
//Todo: remove Test data
        graphInfo.setProperty("nodeCount", graphInfo.size());
        graphInfo.setProperty("buildTime", Duration.between(now, clock.instant()).toMillis());
        cachedGraphs.put(gateway.getId(), graphInfo);
        setNodeCoordinates(graphInfo);
        return graphInfo;
    }

//    public GraphInfo from(DeviceTopology deviceTopology) {
//        Instant now = clock.instant();
//        Optional<GraphInfo> existing = cachedGraphs.stream().filter(g -> g.getRootNode().getId() == deviceTopology.getRoot().getId()).findFirst();
//        if (existing.isPresent()) {
//            if (existing.get().isValid(deviceTopology.getPeriod())){
//                GraphInfo graphInfo = existing.get();
//                graphInfo.setProperty("nodeCount", graphInfo.size());
//                graphInfo.setProperty("buildTime", Duration.between(now, clock.instant()).toMillis());
//                return graphInfo;
//            }
//            cachedGraphs.remove(existing.get());
//        }
//        final DeviceNodeInfo rootNode = newNode(deviceTopology.getRoot(), Optional.empty());
//        GraphInfo<Device> graphInfo = new GraphInfo<>(this.graphLayerService, deviceTopology.getPeriod());
//        graphInfo.setRootNode(rootNode);
//        topologyService.getUniqueG3CommunicationPathSegments(deviceTopology.getDevices()).forEach(s ->
//                graphInfo.addNode(newNode(s.getTarget(), Optional.of(s.getSource())))
//        );
////Todo: remove Test data
//        graphInfo.setProperty("nodeCount", graphInfo.size());
//        graphInfo.setProperty("buildTime", Duration.between(now, clock.instant()).toMillis());
//        cachedGraphs.add(graphInfo);
//        return graphInfo;
//    }

    /**
     * Searches for a DeviceNodeInfo representing the device in all cached graphs
     *
     * @param device to find the node for
     * @return an Optional holding the found node
     */
    public Optional<DeviceNodeInfo> getNode(Device device) {
        Device gateway = this.topologyService.getPhysicalGateway(device).orElse(device);
        Instant now = clock.instant();
        GraphInfo<Device> cachedGraphInfo = cachedGraphs.get(gateway.getId());
        if (cachedGraphInfo != null) {
            if (cachedGraphInfo.isValid(now)) {
                return Optional.ofNullable((DeviceNodeInfo) cachedGraphInfo.getNode(device));
            }
        }
        return Optional.empty();
    }

    private DeviceNodeInfo newNode(Device device) {
        final DeviceNodeInfo node = new DeviceNodeInfo(device, Optional.empty(), Optional.empty());
        graphLayerService.getGraphLayers().stream().filter((layer) -> layer.getType() == GraphLayerType.NODE).forEach(node::addLayer);
        return node;
    }

    private DeviceNodeInfo newNode(G3CommunicationPathSegment segment) {
        final DeviceNodeInfo node = new DeviceNodeInfo(segment.getTarget(), Optional.of(segment.getSource()), segment.getInterval() == null ? Optional.empty() : Optional.of(segment.getInterval().toClosedOpenRange()));
        graphLayerService.getGraphLayers().stream().filter((layer) -> layer.getType() == GraphLayerType.NODE).forEach(node::addLayer);
        return node;
    }

    private GraphInfo<Device> setNodeCoordinates(GraphInfo<Device> graphInfo) {
        Map<String, SpatialCoordinates> mapOfParentWithCoordinates = new HashMap<>();
        int i = 1;
        SpatialCoordinates coordinates = ((DeviceNodeInfo) graphInfo.getRootNode()).getDevice().getSpatialCoordinates().orElse(new SpatialCoordinates(new Latitude(new BigDecimal(45.2251093)), new Longitude(new BigDecimal(22.0192515)), new Elevation(BigDecimal.ONE)));
        Collection<NodeInfo<Device>> graphInfoNodes = graphInfo.getNodes();
        for (NodeInfo<Device> nodeInfo : graphInfoNodes) {
            Device parent = nodeInfo.getParent();
            if (parent != null) {
                mapOfParentWithCoordinates.put(parent.getName(), parent.getSpatialCoordinates().orElse(coordinates));
            }
            Device device = ((DeviceNodeInfo) nodeInfo).getDevice();
            if (!device.getSpatialCoordinates().isPresent() && !nodeInfo.isGateway()) {
                nodeInfo.setCoordinates(getCoordinatesInCloseProximityWithParent(graphInfoNodes.size(), i, mapOfParentWithCoordinates.get(nodeInfo.getParent().getName())));
                i++;
            }
        }
        return graphInfo;
    }

    private SpatialCoordinates getCoordinatesInCloseProximityWithParent(int numberOfNodes, int i, SpatialCoordinates coordinates) {
        SpatialCoordinates spatialCoordinates = new SpatialCoordinates();
        double radius = 0.005;
        double slice = 2 * Math.PI / numberOfNodes;
        double angle = slice * i;
        BigDecimal newX = (coordinates.getLatitude().getValue().add(new BigDecimal(radius * Math.cos(angle))));
        BigDecimal newY = (coordinates.getLongitude().getValue().add(new BigDecimal(radius * Math.sin(angle))));
        spatialCoordinates.setLatitude(new Latitude(newX));
        spatialCoordinates.setLongitude(new Longitude(newY));
        spatialCoordinates.setElevation(coordinates.getElevation());
        return spatialCoordinates;
    }



}
