package com.energyict.mdc.device.topology.rest.info;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.topology.rest.GraphLayer;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;

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
public abstract class NodeInfo<T extends HasId> {

    @JsonIgnore
    private T nodeObject;
    @JsonIgnore
    private List<GraphLayer<T>> layers = new ArrayList<>();
    @JsonIgnore
    private Optional<GraphLayer<T>> activeLayer = Optional.empty();

    private NodeInfo parent;
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

    public void setActiveLayer(GraphLayer<T> graphLayer ){
        if (!layers.contains(graphLayer)) {
            throw new IllegalArgumentException("GraphLayer not added to List of layers");
        }
        activeLayer = Optional.of(graphLayer);
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

    public void addChild(NodeInfo<T> info) {
        if (children.add(info)) {
            info.setParent(this);
        }
    }

    public LinkInfo asLinkInfo(){
        if (!isRoot()) {
            return new LinkInfo(this);
        }
        return null;
    }

    public NodeInfo getRoot() {
        if (parent == null) {
            return this;
        }
        return parent.getRoot();
    }
    @JsonGetter
    public long getId() {
        return nodeObject.getId();
    }

    @JsonAnyGetter
    public Map<String, Object> getProperties(){
        Map<String, Object> allProperties =  new HashMap<>();
        if (activeLayer.isPresent()){
            return activeLayer.get().getProperties(this);
        }else{
            layers.parallelStream().map((layer) -> layer.getProperties(this)).forEach(allProperties::putAll);
        }
        return allProperties;
    }

    public NodeInfo getParent() {
        return parent;
    }

    public void setParent(NodeInfo parent) {
        this.parent = parent;
    }
}
