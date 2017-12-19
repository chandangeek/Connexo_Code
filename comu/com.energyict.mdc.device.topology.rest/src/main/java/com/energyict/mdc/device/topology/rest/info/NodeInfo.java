package com.energyict.mdc.device.topology.rest.info;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.geo.SpatialCoordinates;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    protected T nodeObject;
    @JsonIgnore
    protected Map<String, Object> allProperties = new HashMap<>();
    @JsonIgnore
    private List<GraphLayer<T>> layers = new ArrayList<>();
    @JsonIgnore
    private T parent;
    @JsonIgnore
    private SpatialCoordinates coordinates;

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
    @SuppressWarnings("unused")
    @JsonGetter("gateway")
    public boolean isGateway(){
        return parent == null;
    }

    @JsonAnyGetter
    public Map<String, Object> getProperties(){
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

    @Override
    public boolean equals(Object another){
        if (another == null || !(another instanceof NodeInfo) || ((NodeInfo) another).getObjectClass() != this.getObjectClass()){
            return false;
        }
        return (this.nodeObject.getId() == ((NodeInfo) another).nodeObject.getId());
    }

    @Override
    public int hashCode() {
        return (int) (this.nodeObject.getId() ^ (this.nodeObject.getId() >>> 32));
    }

    @JsonIgnore
    public SpatialCoordinates getCoordinates() {
        return coordinates;
    }

    @JsonIgnore
    public void setCoordinates(SpatialCoordinates coordinates) {
        this.coordinates = coordinates;
    }
}
