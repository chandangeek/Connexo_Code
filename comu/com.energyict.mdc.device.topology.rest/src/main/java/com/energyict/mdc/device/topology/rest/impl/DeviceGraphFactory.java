package com.energyict.mdc.device.topology.rest.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.DeviceTopology;
import com.energyict.mdc.device.topology.G3CommunicationPath;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private int nodeCount;

    public DeviceGraphFactory(TopologyService topologyService, GraphLayerService graphLayerService, Clock clock) {
        this.topologyService = topologyService;
        this.graphLayerService = graphLayerService;
        this.clock = clock;
    }

    public GraphInfo from(Device device) {
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
//Todo: remove Test data
        nodeCount = 0;
        Instant now = clock.instant();

        final DeviceNodeInfo rootNode = newNode(deviceTopology.getRoot(), Optional.empty());
        addChilds(rootNode, deviceTopology);
        GraphInfo<Device> graphInfo = new GraphInfo<>(this.graphLayerService);
        graphInfo.setRootNode(rootNode);
//Todo: remove Test data
        graphInfo.setProperty("nodeCount", "" + nodeCount);
        graphInfo.setProperty("buildTime", "" + Duration.between(now, clock.instant()).toMillis());
        return graphInfo;
    }

    public GraphInfo from(G3CommunicationPath communicationPath) {
        nodeCount = 0;
        DeviceNodeInfo rootNode = newNode(communicationPath.getSource(), Optional.empty());
        final List<Device> devicesInCommunicationPath = new ArrayList<>();
        devicesInCommunicationPath.add(rootNode.getDevice());
        devicesInCommunicationPath.addAll(communicationPath.getIntermediateDevices());
        devicesInCommunicationPath.add(communicationPath.getTarget());

        GraphInfo<Device> graphInfo = new GraphInfo<>(this.graphLayerService);
        graphInfo.setRootNode(rootNode);
        for (int i = 1; i < devicesInCommunicationPath.size(); i++) {
            rootNode = newNode(devicesInCommunicationPath.get(i), rootNode);
        }
        graphInfo.setProperty("levelCount", "" + (devicesInCommunicationPath.size() - 1));
        graphInfo.setProperty("nodeCount", "" + nodeCount);

        return graphInfo;
    }


    private void addChilds(final DeviceNodeInfo nodeInfo, DeviceTopology deviceTopology) {
        this.addChilds(nodeInfo, deviceTopology.getDevices());
    }

    private void addChilds(final DeviceNodeInfo nodeInfo, List<Device> devicesInTopology) {
        devicesInTopology.forEach(device -> this.addCommunicationPathNodes(nodeInfo, device));
    }

    private void addCommunicationPathNodes(DeviceNodeInfo nodeInfo, Device device) {
        DeviceNodeInfo root = nodeInfo;
        G3CommunicationPath communicationPath = topologyService.getCommunicationPath(gateway, device);
        List<Device> intermediates = new ArrayList<>(communicationPath.getIntermediateDevices());
        while (!intermediates.isEmpty()) {
            Optional<DeviceNodeInfo> existing = root.findChildNode(intermediates.get(0));
            if (existing.isPresent()) {
                root = existing.get();
            } else {
                root = newNode(intermediates.get(0), root);
            }
            intermediates.remove(0);
        }
        newNode(device, root);
    }

    private DeviceNodeInfo newNode(Device device, DeviceNodeInfo parent) {
        return this.newNode(device, Optional.of(parent));
    }

    private DeviceNodeInfo newNode(Device device, Optional<DeviceNodeInfo> parent) {
        final DeviceNodeInfo node = new DeviceNodeInfo(device);
        graphLayerService.getGraphLayers().stream().filter((layer) -> layer.getType() == GraphLayerType.NODE).forEach(node::addLayer);
        if (parent.isPresent()) {
            if (parent.get().addChild(node)) {
                nodeCount++;
            }
        } else {
            nodeCount++;
        }
        return node;
    }

}
