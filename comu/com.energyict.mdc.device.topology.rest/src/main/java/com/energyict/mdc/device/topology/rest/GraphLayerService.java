package com.energyict.mdc.device.topology.rest;

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

    /**
     * @return all known GraphLayers
     */
    List<GraphLayer> getGraphLayers();

    /**
     * @param name of the layer
     * @return the GraphLayer with given name
     */
    Optional<GraphLayer> getGraphLayer(String name);

    /**
     * @return a list of layers involved in gathering the summary information for a given Node
     */
    List<GraphLayer> getAllSummaryLayers();
}
