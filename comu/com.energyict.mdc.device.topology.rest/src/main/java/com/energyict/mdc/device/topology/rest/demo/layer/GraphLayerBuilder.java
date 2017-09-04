package com.energyict.mdc.device.topology.rest.demo.layer;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.rest.GraphLayer;

/**
 * Helper class for populating the database with 'representative' data for a given network topology
 * Copyrights EnergyICT
 * Date: 31/08/2017
 * Time: 14:58
 */
public interface GraphLayerBuilder {

    boolean isGraphLayerCompatible(GraphLayer layer);
    void buildLayer(Device device);
}
