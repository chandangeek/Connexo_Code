package com.energyict.mdc.device.topology.rest.info;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.topology.rest.GraphLayerService;
import com.energyict.mdc.device.topology.rest.GraphLayerType;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @param <T> type of nodeObject
 * Copyrights EnergyICT
 * Date: 20/12/2016
 * Time: 17:02
 */
@JsonRootName("graph")
@JsonPropertyOrder({"nodes", "links"})
public class GraphInfo<T extends HasId>  {

    private Map<String, Object> properties = new HashMap<>();
    private final GraphLayerService graphLayerService;
    private NodeInfo<T> rootNode;
    @JsonProperty()
    private Set<NodeInfo<T>> nodes = new HashSet<>();
    @JsonProperty()
    private List<LinkInfo<T>> links = new ArrayList<>();
    private Instant birthDay;

    public GraphInfo(GraphLayerService graphLayerService, Instant birthDay) {
        this.graphLayerService = graphLayerService;
        this.birthDay = birthDay;
    }

    public int size(){
        return nodes.size();
    }

    public boolean addNode(NodeInfo<T> node){
      LinkInfo<T> linkInfo = asLinkInfo(node);
      return nodes.add(node) && (linkInfo == null || links.add(linkInfo));
    }

    public NodeInfo<T> getRootNode() {
        return rootNode;
    }

    public void setRootNode(NodeInfo<T> rootNode) {
        this.addNode(rootNode);
        this.rootNode = rootNode;
    }

    /**
     * Can we consider the graphInfo still valid for given Period
     * @param when instant for which we check the validity (time to life = 1h)
     * @return if the current graph can be considered being valid for give period
     */
    public boolean isValid(Instant when){
        Range<Instant> validityPeriod = Range.atMost(this.birthDay.plus(ChronoUnit.HOURS.getDuration()));
        return validityPeriod.contains(when);
    }

    public void setProperty(String name, Object value) {
        this.properties.put(name, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getProperties() {
        return properties;
    }

    private LinkInfo<T> asLinkInfo(NodeInfo<T> nodeInfo){
        LinkInfo<T> linkInfo = nodeInfo.asLinkInfo();
        if (linkInfo != null) {
            graphLayerService.getGraphLayers().stream().filter((layer) -> layer.getType() == GraphLayerType.LINK).forEach(linkInfo::addLayer);
        }
        return linkInfo;
    }
}
