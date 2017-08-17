package com.energyict.mdc.device.topology.rest;

import com.energyict.mdc.device.topology.rest.info.NodeInfo;

import java.util.List;
import java.util.Optional;

/**
 * Service providing additional GraphLayers
 * Copyrights EnergyICT
 * Date: 3/01/2017
 * Time: 16:03
 */
@SuppressWarnings("unused")
public interface GraphLayerService {

    String COMPONENT_NAME = "Topology Graph Layers Service";

    /**
     * new GraphLayers need to be registered before becoming available.
     * @param graphLayer to register
     */
    void register(GraphLayer graphLayer);

    /**
     * Making a graphLayer unavailable
     * @param graphLayer to remove
     */
    void unregister(GraphLayer graphLayer);

    List<GraphLayer> getGraphLayers();

    Optional<GraphLayer> getGraphLayer(String name);

}
