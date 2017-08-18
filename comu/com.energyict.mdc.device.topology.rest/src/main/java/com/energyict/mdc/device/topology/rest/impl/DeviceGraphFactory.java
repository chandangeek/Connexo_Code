package com.energyict.mdc.device.topology.rest.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.DeviceTopology;
import com.energyict.mdc.device.topology.G3CommunicationPath;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.rest.GraphFactory;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.GraphLayerService;
import com.energyict.mdc.device.topology.rest.GraphLayerType;
import com.energyict.mdc.device.topology.rest.info.DeviceNodeInfo;
import com.energyict.mdc.device.topology.rest.info.GraphInfo;

import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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

    private Device gateway;

    public DeviceGraphFactory(TopologyService topologyService, GraphLayerService graphLayerService, Clock clock) {
        this.topologyService = topologyService;
        this.graphLayerService = graphLayerService;
        this.clock = clock;
    }

    public GraphInfo from(Device device) {
        //set all layers active in test mode
      //  graphLayerService.getGraphLayers().forEach(GraphLayer::activate);
        this.gateway = this.topologyService.getPhysicalGateway(device).orElse(device);
        if (gateway.getId() == device.getId()) {
            return from(this.topologyService.getPhysicalTopology(gateway, Range.atLeast(clock.instant())));
        } else {
            Instant now = clock.instant();
            // Seems this takes a lot of time
            GraphInfo<Device> graphInfo = from(this.topologyService.getCommunicationPath(gateway, device));
            graphInfo.setProperty("buildTime", "" + Duration.between(now, clock.instant()).toMillis());
            return graphInfo;
        }
    }

    public GraphInfo from(DeviceTopology deviceTopology) {
        Instant now = clock.instant();
        final DeviceNodeInfo rootNode = newNode(deviceTopology.getRoot(), Optional.empty());
        GraphInfo<Device> graphInfo = new GraphInfo<>(this.graphLayerService);
        graphInfo.addNode(rootNode);
        topologyService.getUniqueG3CommunicationPathSegments(deviceTopology.getDevices()).forEach(s -> {
                graphInfo.addNode(newNode(s.getTarget(), Optional.of(s.getSource())));
        });
//Todo: remove Test data
        graphInfo.setProperty("nodeCount", "" + graphInfo.size());
        graphInfo.setProperty("buildTime", "" + Duration.between(now, clock.instant()).toMillis());
        return graphInfo;
    }

    public GraphInfo from(G3CommunicationPath communicationPath) {
        Instant now = clock.instant();
        DeviceNodeInfo rootNode = newNode(communicationPath.getSource(), Optional.empty());
        final List<Device> devicesInCommunicationPath = new ArrayList<>();
        devicesInCommunicationPath.add(rootNode.getDevice());
        devicesInCommunicationPath.addAll(communicationPath.getIntermediateDevices());
        devicesInCommunicationPath.add(communicationPath.getTarget());
        GraphInfo<Device> graphInfo = new GraphInfo<>(this.graphLayerService);
        graphInfo.addNode(rootNode);
        for (int i = 1; i < devicesInCommunicationPath.size(); i++) {
            rootNode = newNode(devicesInCommunicationPath.get(i), Optional.of(rootNode.getDevice()));
            graphInfo.addNode(rootNode);
        }
//Todo: remove Test data
        graphInfo.setProperty("nodeCount", "" + graphInfo.size());
        graphInfo.setProperty("buildTime", "" + Duration.between(now, clock.instant()).toMillis());
        return graphInfo;
    }


    private DeviceNodeInfo newNode(Device device, Optional<Device> parent) {
        final DeviceNodeInfo node = new DeviceNodeInfo(device, parent);
        graphLayerService.getGraphLayers().stream().filter((layer) -> layer.getType() == GraphLayerType.NODE).forEach(node::addLayer);
        return node;
    }

}
