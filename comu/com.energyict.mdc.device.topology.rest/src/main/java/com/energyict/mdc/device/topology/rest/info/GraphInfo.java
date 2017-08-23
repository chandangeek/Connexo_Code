package com.energyict.mdc.device.topology.rest.info;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.topology.rest.GraphLayerService;
import com.energyict.mdc.device.topology.rest.GraphLayerType;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Range;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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

    public GraphInfo(GraphLayerService graphLayerService, Range<Instant> period) {
        this.graphLayerService = graphLayerService;
        this.setPeriod(period);
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

    private void setPeriod(Range<Instant> period){
        if (period != null) {
            setProperty("period", new PeriodInfo(period));
        }else {
            this.properties.remove("period");
        }
    }

    @SuppressWarnings("unchecked")
    @JsonIgnore
    public PeriodInfo getPeriod(){
        return (PeriodInfo) this.properties.get("period");
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

    @JsonTypeName("period")
    public static class PeriodInfo {
        @JsonProperty
        private long start;
        @JsonProperty
        private long end;

        public PeriodInfo(Range<Instant> period){
            if (period.hasLowerBound())
                this.start = period.lowerEndpoint().toEpochMilli();
            if (period.hasUpperBound())
                this.end = period.upperEndpoint().toEpochMilli();
        }
        // for serialization purposes
        public PeriodInfo(){};

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }
    }

}
