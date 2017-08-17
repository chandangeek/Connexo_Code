package com.energyict.mdc.device.topology.rest.info;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.GraphLayerService;
import com.energyict.mdc.device.topology.rest.GraphLayerType;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 * @param <T> type of nodeObject
 * Copyrights EnergyICT
 * Date: 20/12/2016
 * Time: 17:02
 */
@JsonRootName("graph")
@JsonPropertyOrder({"nodes", "links", "nodeCount", "buildTime"})
public class GraphInfo<T extends HasId> {

    private Properties properties = new Properties();
    private final GraphLayerService graphLayerService;
    private NodeInfo<T> rootNode;

    public GraphInfo(GraphLayerService graphLayerService) {
        this.graphLayerService = graphLayerService;
    }

    public void setRootNode(NodeInfo<T> info) {
        this.rootNode = info;
    }

    @JsonGetter("nodes")
    @SuppressWarnings("unused")
    public Set<NodeInfo<T>> getAllNodeInfos() {
        return this.addNodeInfos(new HashSet<>(), this.rootNode);
    }

    private Set<NodeInfo<T>> addNodeInfos(Set<NodeInfo<T>> nodeInfos, NodeInfo<T> nodeInfo) {
        nodeInfos.add(nodeInfo);
        nodeInfo.getChildren().forEach(child -> addNodeInfos(nodeInfos, child));
        return nodeInfos;
    }

    @JsonGetter("links")
    @SuppressWarnings("unused")
    public List<LinkInfo> getLinkInfos() {
        return this.addLinkInfos(new ArrayList<>(), this.rootNode,  graphLayerService.getGraphLayers().stream().filter((layer) -> layer.getType() == GraphLayerType.LINK).findFirst());
    }

    public void setProperty(String name, Object value) {
        this.properties.put(name, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getProperties() {
        HashMap<String, Object> result = new HashMap<>();
        properties.keySet().stream().forEach(key -> result.put((String) key, properties.getProperty((String) key)));
        return result;
    }

    private List<LinkInfo> addLinkInfos(List<LinkInfo> linkInfos, NodeInfo<T> nodeInfo, Optional<GraphLayer> linksLayer) {
        LinkInfo<T> linkInfo = nodeInfo.asLinkInfo();
        if (linkInfo != null) {
            linksLayer.ifPresent(linkInfo::addLayer);
            linkInfos.add(linkInfo);
        }
        nodeInfo.getChildren().forEach(child -> addLinkInfos(linkInfos, child, linksLayer));
        return linkInfos;
    }

}
