package com.energyict.mdc.device.topology.rest.info;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.topology.rest.GraphLayerService;
import com.energyict.mdc.device.topology.rest.GraphLayerType;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

/**
 * @param <T> type of nodeObject
 * Copyrights EnergyICT
 * Date: 20/12/2016
 * Time: 17:02
 */
@JsonRootName(value="graph" )
@JsonPropertyOrder({ "nodes", "links" })
public class GraphInfo<T extends HasId> {

    private final GraphLayerService graphLayerService;
    private NodeInfo<T> rootNode;

    public GraphInfo(GraphLayerService graphLayerService){
        this.graphLayerService = graphLayerService;
    }

    public void setRootNode(NodeInfo<T> info){
        this.rootNode = info;
    }

    @JsonGetter("nodes")
    public List<NodeInfo<T>> getAllNodeInfos(){
       return this.addNodeInfos(new ArrayList<>(), this.rootNode);
    }

    private List<NodeInfo<T>> addNodeInfos(List<NodeInfo<T>> nodeInfos, NodeInfo<T> nodeInfo){
        nodeInfos.add(nodeInfo);
        if (!nodeInfo.isLeaf()) {
            nodeInfo.getChildren().forEach(child -> addNodeInfos(nodeInfos, child ));
        }
        return nodeInfos;
    }

    @JsonGetter("links")
    public List<LinkInfo> getLinkInfos(){
        return this.addLinkInfos(new ArrayList<>(), this.rootNode);
    }

    private List<LinkInfo> addLinkInfos(List<LinkInfo> linkInfos, NodeInfo<T> nodeInfo){
        if (!nodeInfo.isRoot() && nodeInfo.isLeaf()){
           LinkInfo linkInfo = nodeInfo.asLinkInfo();
           if (linkInfo != null) {
               graphLayerService.getGraphLayers().stream().filter((layer) -> layer.getType() == GraphLayerType.LINK).forEach(linkInfo::addLayer);
               linkInfos.add(linkInfo);
           }
        }
        nodeInfo.getChildren().forEach(child -> addLinkInfos(linkInfos, child ));
        return linkInfos;
    }

}
