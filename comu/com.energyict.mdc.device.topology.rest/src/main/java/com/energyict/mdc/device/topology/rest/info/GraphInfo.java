package com.energyict.mdc.device.topology.rest.info;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 20/12/2016
 * Time: 17:02
 */
@JsonRootName(value="graph")
public class GraphInfo {

    private NodeInfo rootNode;

    public void setRootNode(NodeInfo info){
        this.rootNode = info;
    }

    @JsonGetter("nodes")
    public List<NodeInfo> getAllNodeInfos(){
       return this.addNodeInfos(new ArrayList<>(), this.rootNode);
    }

    private List<NodeInfo> addNodeInfos(List<NodeInfo> nodeInfos, NodeInfo nodeInfo){
        nodeInfos.add(nodeInfo);
        if (!nodeInfo.isLeaf()) {
            nodeInfos.add(nodeInfo);
        }
        return nodeInfos;
    }

    @JsonGetter("links")
    public List<LinkInfo> getLinkInfos(){
        return this.addLinkInfos(new ArrayList<>(), this.rootNode);
    }

    private List<LinkInfo> addLinkInfos(List<LinkInfo> linkInfos, NodeInfo nodeInfo){
        if (nodeInfo.isLeaf()){
           linkInfos.add(nodeInfo.asLinkInfo());
        }
        nodeInfo.getChildren().forEach(child -> addLinkInfos(linkInfos, child ));
        return linkInfos;
    }

}
