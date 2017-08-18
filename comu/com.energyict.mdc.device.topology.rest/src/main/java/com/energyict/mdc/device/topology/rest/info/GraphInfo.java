package com.energyict.mdc.device.topology.rest.info;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.GraphLayerService;
import com.energyict.mdc.device.topology.rest.GraphLayerType;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonProperty;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @param <T> type of nodeObject
 * Copyrights EnergyICT
 * Date: 20/12/2016
 * Time: 17:02
 */
@JsonIgnoreType
@JsonPropertyOrder({"nodes", "links"})
public class GraphInfo<T extends HasId> {

    private Properties properties = new Properties();
    private final GraphLayerService graphLayerService;
    @JsonProperty()
    private Set<NodeInfo<T>> nodes = new HashSet<>();

    public GraphInfo(GraphLayerService graphLayerService) {
        this.graphLayerService = graphLayerService;
    }

    public int size(){
        return nodes.size();
    }

    public boolean addNode(NodeInfo<T> node){
        return this.nodes.add(node);
    }

    @JsonGetter("links")
    @SuppressWarnings("unused")
    private List<LinkInfo> getLinkInfos() {
        return this.nodes.stream().filter((node) -> !node.isRoot()).map(this::asLinkInfo).collect(Collectors.toList());
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

    private LinkInfo<T> asLinkInfo(NodeInfo<T> nodeInfo){
        LinkInfo<T> linkInfo = nodeInfo.asLinkInfo();
        if (linkInfo != null) {
            graphLayerService.getGraphLayers().stream().filter((layer) -> layer.getType() == GraphLayerType.LINK).forEach(linkInfo::addLayer);
        }
        return linkInfo;
    }

}
