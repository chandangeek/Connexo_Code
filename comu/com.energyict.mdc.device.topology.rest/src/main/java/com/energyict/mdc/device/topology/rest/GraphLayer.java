package com.energyict.mdc.device.topology.rest;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.topology.rest.info.NodeInfo;

import java.util.Map;

/**
 * GraphLayer represents the data used by a layer on a Graph where particular properties of devices or links will be shown.
 * The GraphLayerType specifies wether the properties will be used as properties of the NODE, either as properties of the LINKS
 * @param <T> Type of objects this layer can handle
 * Copyrights EnergyICT
 * Date: 3/01/2017
 * Time: 11:10
 */
public interface GraphLayer<T extends HasId> {

    /**
     * To differentiate properties set on nodes and properties set on links
     * @return the type of the layer
     */
    GraphLayerType getType();

    /**
     * Each layer should have a different name, so differentiate them
     * @return the name of the layer
     */
    String getName();

    /**
     *
     * @return a Map of properties to be shown on this layer
     */
    Map<String, Object> getProperties(NodeInfo<T> info);

    void getProperty(String propertyName);
    void setProperty(String propertyName, Object propertyValue);

}
