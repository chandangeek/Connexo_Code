package com.energyict.mdc.device.topology.rest.info;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.topology.rest.GraphLayerService;
import com.energyict.mdc.device.topology.rest.GraphLayerType;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
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
public class GraphInfo<T extends HasId> implements StreamingOutput {

    private Properties properties = new Properties();
    private final GraphLayerService graphLayerService;
    @JsonProperty()
    private Set<NodeInfo<T>> nodes = new HashSet<>();
    @JsonProperty()
    private List<LinkInfo<T>> links = new ArrayList<>();

    public GraphInfo(GraphLayerService graphLayerService) {
        this.graphLayerService = graphLayerService;
    }

    public int size(){
        return nodes.size();
    }

    public boolean addNode(NodeInfo<T> node){
      LinkInfo<T> linkInfo = asLinkInfo(node);
      return nodes.add(node) && (linkInfo == null || links.add(linkInfo));
    }

//    public void removeNode(NodeInfo<T> node){
//        links.stream().filter(l -> l.nodeInfo == node).findFirst().ifPresent(links::remove);
//        nodes.stream().filter(n -> n == node).findFirst().ifPresent(nodes::remove);
//    }

    @Override
    public void write(OutputStream outputStream) throws IOException, WebApplicationException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writer().writeValue(outputStream, this);
        outputStream.flush();
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
