package com.energyict.mdc.device.topology.rest.info;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.rest.GraphLayer;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a Device node in a network.
 * Date: 20/12/2016
 * Time: 16:57
 */
@JsonIgnoreType
public class NodeInfo {

    private long id;

    @JsonIgnore
    private List<GraphLayer> layers = new ArrayList<>();
    @JsonIgnore
    private Optional<GraphLayer> activeLayer;

    private NodeInfo parent;
    private List<NodeInfo> children = new ArrayList<>();

    public NodeInfo(Device device) {
        this.id = device.getId();
        DeviceInfoLayer deviceInfoLayer = new DeviceInfoLayer(device);
        this.addLayer(deviceInfoLayer);
        this.setActiveLayer(deviceInfoLayer);
    }

    public boolean addLayer(GraphLayer graphLayer){
        return this.layers.add(graphLayer);
    }

    public void setActiveLayer(GraphLayer graphLayer ){
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
    public List<NodeInfo> getChildren(){
        return Collections.unmodifiableList(children);
    }

    public void addChild(NodeInfo info) {
        if (children.add(info)) {
            info.setParent(this);
        }
    }

    public LinkInfo asLinkInfo(){
        if (!isRoot()) {
            LinkInfo linkInfo = new LinkInfo(parent.getId(), this.getId());
            LinkQualityLayer linkQualityLayer = new LinkQualityLayer(0);
            linkInfo.addLayer(linkQualityLayer);
            linkInfo.setActiveLayer(linkQualityLayer);
            return linkInfo;
        }
        return null;
    }

    public NodeInfo getRoot() {
        if (parent == null) {
            return this;
        }
        return parent.getRoot();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @JsonAnyGetter
    public Map<String, Object> getProperties(){
        if (activeLayer.isPresent()){
            return activeLayer.get().getProperties();
        }
        return new HashMap<>();
    }

    public boolean isGateWay() {
        return this.getParent() == null;
    }

    public NodeInfo getParent() {
        return parent;
    }

    public void setParent(NodeInfo parent) {
        this.parent = parent;
    }
}
