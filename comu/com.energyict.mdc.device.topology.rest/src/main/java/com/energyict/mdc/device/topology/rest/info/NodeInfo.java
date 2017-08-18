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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a (T) node in a network.
 * @param <T> type of nodeObject
 * Date: 20/12/2016
 * Time: 16:57
 */
@JsonIgnoreType
@JsonPropertyOrder({ "id", "gateway"})
public abstract class NodeInfo<T extends HasId> {

    @JsonIgnore
    private T nodeObject;
    @JsonIgnore
    private List<GraphLayer<T>> layers = new ArrayList<>();
    @JsonIgnore
    private T parent;
    @JsonIgnore
    private List<NodeInfo<T>> children = new ArrayList<>();

    public NodeInfo(T nodeObject) {
        this.nodeObject = nodeObject;
    }

    @JsonIgnore
    abstract Class getObjectClass();

    T getNodeObject(){
      return nodeObject;
    }

    public boolean addLayer(GraphLayer<T> graphLayer){
        return this.layers.add(graphLayer);
    }

    @JsonIgnore
    public boolean isRoot(){
        return this.parent == null;
    }
    @JsonIgnore
    public boolean isLeaf(){
        return children.isEmpty();
    }
    @JsonIgnore
    public List<NodeInfo<T>> getChildren(){
        return Collections.unmodifiableList(children);
    }

    public LinkInfo<T> asLinkInfo(){
        if (!isRoot()) {
            return new LinkInfo<>(this);
        }
        return null;
    }

    @JsonGetter("id")
    public long getId() {
        return nodeObject.getId();
    }
    @JsonGetter("gateway")
    public boolean isGateway(){
        return parent == null;
    }

    @JsonAnyGetter
    public Map<String, Object> getProperties(){
        Map<String, Object> allProperties =  new HashMap<>();
        layers.stream().filter(GraphLayer::isActive).forEach(layer -> allProperties.putAll(layer.getProperties(this)));
        return allProperties;
    }
    @JsonIgnore
    public T getParent() {
        return parent;
    }

    public void setParent(T parent) {
        this.parent = parent;
    }
}
