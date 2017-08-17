package com.energyict.mdc.device.topology.rest.info;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.GraphLayerCalculationMode;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Represents the link (path) between two nodes in a network.
 * Date: 20/12/2016
 * Time: 16:57
 */
@JsonIgnoreType
@JsonPropertyOrder({ "source", "target" })
public class LinkInfo<T extends HasId> {

    @JsonIgnore
    private NodeInfo<T> nodeInfo;
    private long source;
    private long target;
    @JsonIgnore
    private List<GraphLayer<T>> layers = new ArrayList<>();

    LinkInfo(NodeInfo<T> nodeInfo){
        this(nodeInfo.getParent().getId(), nodeInfo.getId());
        this.nodeInfo = nodeInfo;
    }

    private LinkInfo(long source, long target){
       this.source = source;
       this.target = target;
    }

    public boolean addLayer(GraphLayer<T> graphLayer){
        return this.layers.add(graphLayer);
    }

    @JsonGetter("source")
    public long getSource() {
        return source;
    }

    @JsonGetter("target")
    public long getTarget() {
        return target;
    }

    @JsonAnyGetter
    public Map<String, Object> getProperties(){
        Map<String, Object> allProperties =  new HashMap<>();
        layers.stream().filter(layer -> layer.getCalculationMode() == GraphLayerCalculationMode.IMMEDIATE).forEach(layer -> allProperties.putAll(layer.getProperties(nodeInfo)));
        return allProperties;
    }

}
