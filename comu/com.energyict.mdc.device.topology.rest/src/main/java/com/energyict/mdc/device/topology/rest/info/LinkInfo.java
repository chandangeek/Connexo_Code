package com.energyict.mdc.device.topology.rest.info;

import com.energyict.mdc.device.topology.rest.GraphLayer;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;

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
public class LinkInfo {

    private long source;
    private long target;
    @JsonIgnore
    private List<GraphLayer> layers = new ArrayList<>();
    @JsonIgnore
    private Optional<GraphLayer> activeLayer;

    LinkInfo(long source, long target){
       this.source = source;
       this.target = target;
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

    @JsonGetter
    public long getSource() {
        return source;
    }

    public void setSource(long source) {
        this.source = source;
    }
    @JsonGetter
    public long getTarget() {
        return target;
    }

    public void setTarget(long target) {
        this.target = target;
    }

    @JsonAnyGetter
    public Map<String, Object> getProperties(){
        if (activeLayer.isPresent()){
            return activeLayer.get().getProperties();
        }
        return new HashMap<>();
    }

}
