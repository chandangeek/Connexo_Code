package com.energyict.mdc.device.topology.rest.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.rest.GraphFactory;
import com.energyict.mdc.device.topology.rest.info.GraphInfo;
import com.energyict.mdc.device.topology.rest.info.LinkInfo;
import com.energyict.mdc.device.topology.rest.info.NodeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 20/12/2016
 * Time: 17:16
 */

public class DefaultGraphFactory implements GraphFactory{


    private TopologyService topologyService;
    private GraphInfo graphInfo;

    public DefaultGraphFactory(TopologyService topologyService){
       this.topologyService = topologyService;
    }

    public GraphInfo from(Device device){
        this.graphInfo = new GraphInfo();
        Device gateway = this.topologyService.getPhysicalGateway(device).orElse(device);

        NodeInfo rootNode = new NodeInfo(gateway);
        this.graphInfo.setRootNode(rootNode);
        this.topologyService.findPhysicalConnectedDevices(gateway).stream().forEach(each -> rootNode.addChild(new NodeInfo(each)));
        return graphInfo;
    }


}
