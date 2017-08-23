package com.energyict.mdc.device.topology.rest.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.DeviceTopology;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.rest.GraphFactory;
import com.energyict.mdc.device.topology.rest.GraphLayerService;
import com.energyict.mdc.device.topology.rest.GraphLayerType;
import com.energyict.mdc.device.topology.rest.info.DeviceNodeInfo;
import com.energyict.mdc.device.topology.rest.info.GraphInfo;

import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private List<GraphInfo> cachedGraphs = new ArrayList<>();

    public DeviceGraphFactory(TopologyService topologyService, GraphLayerService graphLayerService, Clock clock) {
        this.topologyService = topologyService;
        this.graphLayerService = graphLayerService;
        this.clock = clock;
    }

    public GraphInfo from(Device device) {
        Device gateway = this.topologyService.getPhysicalGateway(device).orElse(device);
        if (gateway.getId() == device.getId()) {
            return from(this.topologyService.getPhysicalTopology(gateway, Range.atLeast(clock.instant())));
        } else {
            Instant now = clock.instant();
            final DeviceNodeInfo rootNode = newNode(gateway, Optional.empty());
            GraphInfo<Device> graphInfo = new GraphInfo<>(this.graphLayerService, null);
            graphInfo.setRootNode(rootNode);
            topologyService.getUniqueG3CommunicationPathSegments(Collections.singletonList(device)).forEach(s ->
                    graphInfo.addNode(newNode(s.getTarget(), Optional.of(s.getSource()))));
//Todo: remove Test data
            graphInfo.setProperty("nodeCount", graphInfo.size());
            graphInfo.setProperty("buildTime", Duration.between(now, clock.instant()).toMillis());
            return graphInfo;
        }
    }

    public GraphInfo from(DeviceTopology deviceTopology) {
        Instant now = clock.instant();
        Optional<GraphInfo> existing = cachedGraphs.stream().filter(g -> g.getRootNode().getId() == deviceTopology.getRoot().getId()).findFirst();
        if (existing.isPresent()) {
            if (existing.get().getPeriod().getStart() == deviceTopology.getPeriod().lowerEndpoint().toEpochMilli()){
                return existing.get();
            }
            cachedGraphs.remove(existing.get());
        }
        final DeviceNodeInfo rootNode = newNode(deviceTopology.getRoot(), Optional.empty());
        GraphInfo<Device> graphInfo = new GraphInfo<>(this.graphLayerService, deviceTopology.getPeriod());
        graphInfo.setRootNode(rootNode);
        topologyService.getUniqueG3CommunicationPathSegments(deviceTopology.getDevices()).forEach(s ->
                graphInfo.addNode(newNode(s.getTarget(), Optional.of(s.getSource())))
        );
//Todo: remove Test data
        graphInfo.setProperty("nodeCount", graphInfo.size());
        graphInfo.setProperty("buildTime", Duration.between(now, clock.instant()).toMillis());
        cachedGraphs.add(graphInfo);
        return graphInfo;
    }

    private DeviceNodeInfo newNode(Device device, Optional<Device> parent) {
        final DeviceNodeInfo node = new DeviceNodeInfo(device, parent);
        graphLayerService.getGraphLayers().stream().filter((layer) -> layer.getType() == GraphLayerType.NODE).forEach(node::addLayer);
        return node;
    }

}
