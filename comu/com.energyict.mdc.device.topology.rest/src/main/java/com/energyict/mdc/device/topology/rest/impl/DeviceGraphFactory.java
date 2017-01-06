package com.energyict.mdc.device.topology.rest.impl;

import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.DeviceTopology;
import com.energyict.mdc.device.topology.G3CommunicationPath;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.rest.GraphFactory;
import com.energyict.mdc.device.topology.rest.GraphLayerService;
import com.energyict.mdc.device.topology.rest.GraphLayerType;
import com.energyict.mdc.device.topology.rest.info.DeviceNodeInfo;
import com.energyict.mdc.device.topology.rest.info.GraphInfo;
import com.energyict.mdc.device.topology.rest.info.NodeInfo;

import com.google.common.collect.Range;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 20/12/2016
 * Time: 17:16
 */

public class DeviceGraphFactory implements GraphFactory{

    private final TopologyService topologyService;
    private final GraphLayerService graphLayerService;
    private final Clock clock;

    private Device gateway;

    public DeviceGraphFactory(TopologyService topologyService, GraphLayerService graphLayerService, Clock clock){
        this.topologyService = topologyService;
        this.graphLayerService = graphLayerService;
        this.clock = clock;
    }

    public GraphInfo from(Device device){
        this.gateway = this.topologyService.getPhysicalGateway(device).orElse(device);
        return from(this.topologyService.getPhysicalTopology(gateway, Range.atLeast(clock.instant())));
    }

    public GraphInfo from(DeviceTopology deviceTopology){
        final DeviceNodeInfo rootNode = newNode(deviceTopology.getRoot(), Optional.empty());

        GraphInfo<Device> graphInfo = new GraphInfo<>(this.graphLayerService);
        graphInfo.setRootNode(rootNode);
        addChilds(rootNode, deviceTopology);
        return graphInfo;
    }

    private void addChilds(final DeviceNodeInfo nodeInfo, DeviceTopology deviceTopology){
        List<DeviceTopology> children = new ArrayList<>(deviceTopology.getChildren());
        deviceTopology.getDevices().forEach(device -> this.addCommunicationPathNodes(nodeInfo, device));
        children.stream().filter(Predicates.not(DeviceTopology::isLeaf)).forEach(child -> addChilds(nodeInfo, child));
    }

    private void addCommunicationPathNodes(DeviceNodeInfo nodeInfo, Device device){
        DeviceNodeInfo root = nodeInfo;
        G3CommunicationPath communicationPath = topologyService.getCommunicationPath(gateway, device);
        List<Device> intermediates = new ArrayList<>(communicationPath.getIntermediateDevices());
        while (!intermediates.isEmpty()) {
            root = newNode(intermediates.get(0), Optional.of(root));
            intermediates.remove(0);
        }
        newNode(device, Optional.of(root));
    }

    private DeviceNodeInfo newNode(Device device, Optional<DeviceNodeInfo> parent){
        final DeviceNodeInfo node = new DeviceNodeInfo(device);
        graphLayerService.getGraphLayers().stream().filter((layer) -> layer.getType() == GraphLayerType.NODE).forEach(node::addLayer);
        if (parent.isPresent()){
            parent.get().addChild(node);
        }
        return node;
    }

}
