package com.energyict.mdc.device.topology.rest.impl;

import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.DeviceTopology;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.rest.GraphFactory;
import com.energyict.mdc.device.topology.rest.GraphLayerService;
import com.energyict.mdc.device.topology.rest.info.GraphInfo;
import com.energyict.mdc.device.topology.rest.info.NodeInfo;

import com.google.common.collect.Range;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 20/12/2016
 * Time: 17:16
 */

public class DefaultGraphFactory implements GraphFactory{

    private final TopologyService topologyService;
    private final GraphLayerService graphLayerService;
    private final Clock clock;

    private Device gateway;

    public DefaultGraphFactory(TopologyService topologyService, GraphLayerService graphLayerService, Clock clock){
        this.topologyService = topologyService;
        this.graphLayerService = graphLayerService;
        this.clock = clock;
    }

    public GraphInfo from(Device device){
        this.gateway = this.topologyService.getPhysicalGateway(device).orElse(device);

//        this.graphInfo = new GraphInfo();
//        NodeInfo rootNode = new NodeInfo(gateway);
//        this.graphInfo.setRootNode(rootNode);
//        this.topologyService.findPhysicalConnectedDevices(gateway).stream().forEach(each -> rootNode.addChild(new NodeInfo(each)));

        return from(this.topologyService.getPhysicalTopology(gateway, Range.atLeast(clock.instant())));
    }

    public GraphInfo from(DeviceTopology deviceTopology){
        final NodeInfo rootNode = new NodeInfo(deviceTopology.getRoot());
        graphLayerService.getGraphLayers().stream().forEach(rootNode::addLayer);

        GraphInfo graphInfo = new GraphInfo();
        graphInfo.setRootNode(rootNode);
        addChilds(rootNode, deviceTopology);
        return graphInfo;
    }

    private void addChilds(final NodeInfo nodeInfo, DeviceTopology deviceTopology){
        List<DeviceTopology> children = new ArrayList<>(deviceTopology.getChildren());
        deviceTopology.getDevices().forEach(device -> this.addCommunicationPathNodes(nodeInfo, device));
        children.stream().filter(Predicates.not(DeviceTopology::isLeaf)).forEach(child -> addChilds(nodeInfo, child));
    }

    private void addCommunicationPathNodes(NodeInfo nodeInfo, Device device){
        NodeInfo root = nodeInfo;
        List<Device> intermediates = new ArrayList<>(topologyService.getCommunicationPath(gateway, device).getIntermediateDevices());
        while (!intermediates.isEmpty()) {
            final NodeInfo child = new NodeInfo(intermediates.get(0));
            graphLayerService.getGraphLayers().stream().forEach(child::addLayer);
            root.addChild(child);
            root = child;
            intermediates.remove(0);
        }
        final NodeInfo leaf = new NodeInfo(device);
        graphLayerService.getGraphLayers().stream().forEach(leaf::addLayer);
        root.addChild(new NodeInfo(device));
    }

}
